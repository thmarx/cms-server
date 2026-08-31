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

import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;

/** Fluent configuration for one binding. */
public interface BindingBuilder<T> {
    BindingBuilder<T> named(String name);

    BindingBuilder<T> to(Class<? extends T> implementation);

    void toInstance(T instance);

    BindingBuilder<T> toProvider(Provider<? extends T> provider);

    BindingBuilder<T> singleton();

    default BindingBuilder<T> in(Class<? extends Annotation> scope) {
        if (scope != Singleton.class) {
            throw new DIException("Only jakarta.inject.Singleton is supported");
        }
        return singleton();
    }
}
