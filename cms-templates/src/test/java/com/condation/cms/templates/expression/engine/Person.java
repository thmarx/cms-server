package com.condation.cms.templates.expression.engine;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

public class Person {
    private final String name;
    private final int age;
    private final Address wohnort;

    public Person(String name, int age, Address wohnort) {
        this.name = name;
        this.age = age;
        this.wohnort = wohnort;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Address getWohnort() {
        return wohnort;
    }
}
