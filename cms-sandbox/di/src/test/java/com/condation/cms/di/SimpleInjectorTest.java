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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class SimpleInjectorTest {
    @Test
    void modulesProvideNamedSingletonsAndConstructorInjection() {
        Injector injector = DI.createInjector(new StorageModule(), new ApplicationModule());

        Application first = injector.getInstance(Application.class);
        Application second = injector.getInstance(Application.class);

        assertNotSame(first, second);
        assertSame(first.store, second.store);
        assertEquals("content", first.directory);
        assertSame(injector, first.injector);
    }

    @Test
    void explicitBindingsAndJakartaSingletonAreSupported() {
        Injector injector = DI.createInjector(binder -> {
            binder.bind(Store.class).to(MemoryStore.class).singleton();
            binder.bind(String.class).named("content").toInstance("instance-value");
        });

        assertSame(injector.getInstance(Store.class), injector.getInstance(Store.class));
        assertEquals("instance-value", injector.getInstance(String.class, "content"));
        assertSame(injector.getInstance(AnnotatedSingleton.class), injector.getInstance(AnnotatedSingleton.class));
    }

    @Test
    void childOverridesParentWhileKeepingParentBindings() {
        Injector parent = DI.createInjector(binder -> {
            binder.bind(Store.class).to(MemoryStore.class).singleton();
            binder.bind(String.class).named("content").toInstance("parent");
        });
        Injector child = parent.createChildInjector(binder ->
                binder.bind(String.class).named("content").toInstance("child"));

        assertEquals("parent", parent.getInstance(String.class, "content"));
        assertEquals("child", child.getInstance(String.class, "content"));
        assertSame(parent.getInstance(Store.class), child.getInstance(Store.class));
    }

    @Test
    void fieldAndMethodInjectionAreIgnored() {
        Injector injector = DI.createInjector(new StorageModule());
        ExternalObject object = injector.getInstance(ExternalObject.class);

        assertFalse(object.baseInjected);
        assertNull(object.directory);
    }

    @Test
    void reportsMissingAndCircularDependencies() {
        Injector injector = DI.createInjector();

        assertThrows(DIException.class, () -> injector.getInstance(Store.class));
        DIException cycle = assertThrows(DIException.class, () -> injector.getInstance(First.class));
        assertTrue(cycle.getMessage().contains("Circular dependency"));
    }

    interface Store {
    }

    static final class MemoryStore implements Store {
    }

    static final class Application {
        final Store store;
        final String directory;
        final Injector injector;

        @Inject
        Application(Store store, @Named("content") String directory, Injector injector) {
            this.store = store;
            this.directory = directory;
            this.injector = injector;
        }
    }

    static final class StorageModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(Store.class).to(MemoryStore.class).singleton();
        }

        @Provides
        @Singleton
        @Named("content")
        String directory() {
            return "content";
        }
    }

    static final class ApplicationModule implements Module {
        @Override
        public void configure(Binder binder) {
            // Provider-only module.
        }
    }

    @Singleton
    static final class AnnotatedSingleton {
    }

    static class ExternalBase {
        boolean baseInjected;

        @Inject
        void initialize(Store store) {
            baseInjected = store != null;
        }
    }

    static final class ExternalObject extends ExternalBase {
        @Inject
        @Named("content")
        String directory;
    }

    static final class First {
        @Inject
        First(Second second) {
        }
    }

    static final class Second {
        @Inject
        Second(First first) {
        }
    }
}
