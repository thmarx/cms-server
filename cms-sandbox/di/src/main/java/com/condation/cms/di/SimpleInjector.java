package com.condation.cms.di;

/*-
 * #%L
 * CMS minimal DI sandbox
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

final class SimpleInjector implements Injector, Binder {
    private final SimpleInjector parent;
    private final Map<Key, Binding<?>> bindings = new ConcurrentHashMap<>();
    private final ThreadLocal<Deque<Key>> resolutionPath = ThreadLocal.withInitial(ArrayDeque::new);

    SimpleInjector(SimpleInjector parent, Module... modules) {
        this.parent = parent;
        Module[] configuredModules = modules == null ? new Module[0] : modules.clone();
        for (Module module : configuredModules) {
            Objects.requireNonNull(module, "module").configure(this);
        }
        for (Module module : configuredModules) {
            registerProviderMethods(module);
        }
    }

    @Override
    public <T> BindingBuilder<T> bind(Class<T> type) {
        Objects.requireNonNull(type, "type");
        Binding<T> binding = new Binding<>(new Key(type, null), injector -> injector.construct(type));
        put(binding);
        return new Builder<>(binding);
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return getInstance(type, null);
    }

    @Override
    public <T> T getInstance(Class<T> type, String name) {
        Objects.requireNonNull(type, "type");
        Key key = new Key(type, normalizeName(name));
        if (key.name == null && type == Injector.class) {
            return type.cast(this);
        }

        Binding<?> binding = bindings.get(key);
        if (binding != null) {
            return type.cast(resolve(key, binding));
        }
        if (parent != null && parent.hasBinding(key)) {
            return parent.getInstance(type, key.name);
        }
        if (key.name != null) {
            throw new DIException("No binding for " + key);
        }
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw new DIException("No binding for " + type.getName());
        }

        Binding<T> justInTime = new Binding<>(key, injector -> injector.construct(type));
        justInTime.singleton = type.isAnnotationPresent(Singleton.class);
        Binding<?> existing = bindings.putIfAbsent(key, justInTime);
        return type.cast(resolve(key, existing == null ? justInTime : existing));
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        return new SimpleInjector(this, modules);
    }

    private boolean hasBinding(Key key) {
        return bindings.containsKey(key) || parent != null && parent.hasBinding(key);
    }

    private Object resolve(Key key, Binding<?> binding) {
        Deque<Key> path = resolutionPath.get();
        if (path.contains(key)) {
            List<Key> cycle = new ArrayList<>(path);
            Collections.reverse(cycle);
            cycle.add(key);
            throw new DIException("Circular dependency: " + String.join(" -> ", cycle.stream().map(Key::toString).toList()));
        }
        path.push(key);
        try {
            return binding.get(this);
        } finally {
            path.pop();
            if (path.isEmpty()) {
                resolutionPath.remove();
            }
        }
    }

    private <T> T construct(Class<T> implementation) {
        Constructor<T> constructor = injectionConstructor(implementation);
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(resolveParameters(constructor.getParameters()));
        } catch (InvocationTargetException exception) {
            throw creationFailure(implementation.getName(), exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw creationFailure(implementation.getName(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> injectionConstructor(Class<T> type) {
        List<Constructor<?>> injected = Arrays.stream(type.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                .toList();
        if (injected.size() > 1) {
            throw new DIException("Multiple @Inject constructors on " + type.getName());
        }
        if (injected.size() == 1) {
            return (Constructor<T>) injected.getFirst();
        }
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException exception) {
            throw new DIException("No @Inject or no-arg constructor on " + type.getName());
        }
    }

    private Object[] resolveParameters(Parameter[] parameters) {
        Object[] values = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index++) {
            values[index] = dependency(parameters[index].getType(), parameters[index]);
        }
        return values;
    }

    private Object dependency(Class<?> type, AnnotatedElement injectionPoint) {
        Named named = injectionPoint.getAnnotation(Named.class);
        return getInstance(type, named == null ? null : named.value());
    }

    private void registerProviderMethods(Module module) {
        for (Method method : module.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Provides.class)) {
                continue;
            }
            if (method.getReturnType() == void.class) {
                throw new DIException("@Provides method must return a value: " + method);
            }
            Named named = method.getAnnotation(Named.class);
            Key key = new Key(method.getReturnType(), named == null ? null : normalizeName(named.value()));
            Binding<Object> binding = new Binding<>(key, injector -> invokeProvider(module, method));
            binding.singleton = method.isAnnotationPresent(Singleton.class)
                    || method.getReturnType().isAnnotationPresent(Singleton.class);
            put(binding);
        }
    }

    private Object invokeProvider(Module module, Method method) {
        try {
            method.setAccessible(true);
            Object value = method.invoke(module, resolveParameters(method.getParameters()));
            if (value == null) {
                throw new DIException("@Provides method returned null: " + method);
            }
            return value;
        } catch (InvocationTargetException exception) {
            throw creationFailure(method.toString(), exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw creationFailure(method.toString(), exception);
        }
    }

    private void put(Binding<?> binding) {
        if (bindings.putIfAbsent(binding.key, binding) != null) {
            throw new DIException("Duplicate binding for " + binding.key);
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new DIException("A binding name must not be blank");
        }
        return normalized;
    }

    private static DIException creationFailure(String target, Throwable cause) {
        if (cause instanceof DIException diException) {
            return diException;
        }
        return new DIException("Could not create or inject " + target, cause);
    }

    private final class Builder<T> implements BindingBuilder<T> {
        private final Binding<T> binding;

        private Builder(Binding<T> binding) {
            this.binding = binding;
        }

        @Override
        public BindingBuilder<T> named(String name) {
            Key oldKey = binding.key;
            Key newKey = new Key(oldKey.type, normalizeName(name));
            if (!bindings.remove(oldKey, binding)) {
                throw new DIException("Binding is no longer configurable: " + oldKey);
            }
            binding.key = newKey;
            put(binding);
            return this;
        }

        @Override
        public BindingBuilder<T> to(Class<? extends T> implementation) {
            Objects.requireNonNull(implementation, "implementation");
            binding.factory = injector -> injector.construct(implementation);
            binding.singleton |= implementation.isAnnotationPresent(Singleton.class);
            return this;
        }

        @Override
        public void toInstance(T instance) {
            Objects.requireNonNull(instance, "instance");
            binding.factory = ignored -> instance;
            binding.singleton = true;
            binding.instance = instance;
        }

        @Override
        public BindingBuilder<T> singleton() {
            binding.singleton = true;
            return this;
        }
    }

    @FunctionalInterface
    private interface Factory<T> {
        T create(SimpleInjector injector);
    }

    private static final class Binding<T> {
        private static final Object UNINITIALIZED = new Object();

        private Key key;
        private Factory<? extends T> factory;
        private boolean singleton;
        private volatile Object instance = UNINITIALIZED;

        private Binding(Key key, Factory<? extends T> factory) {
            this.key = key;
            this.factory = factory;
        }

        private T get(SimpleInjector injector) {
            if (!singleton) {
                return factory.create(injector);
            }
            Object current = instance;
            if (current == UNINITIALIZED) {
                synchronized (this) {
                    current = instance;
                    if (current == UNINITIALIZED) {
                        current = factory.create(injector);
                        instance = current;
                    }
                }
            }
            @SuppressWarnings("unchecked")
            T result = (T) current;
            return result;
        }
    }

    private static final class Key {
        private final Class<?> type;
        private final String name;

        private Key(Class<?> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Key other && type.equals(other.type) && Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }

        @Override
        public String toString() {
            return name == null ? type.getName() : type.getName() + " named '" + name + "'";
        }
    }
}
