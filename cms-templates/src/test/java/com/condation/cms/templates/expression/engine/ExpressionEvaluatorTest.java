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

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class ExpressionEvaluatorTest {
	
	
	@Test
	void simple () {
		ExpressionEvaluator evaluator = new ExpressionEvaluator();

        evaluator.setVariable("x", 10);
        evaluator.setVariable("y", 5);
        evaluator.setVariable("name", "Alice");

        Address wohnort = new Address("Hauptstraße", 12345);
        Person person = new Person("Bob", 25, wohnort);
        evaluator.setVariable("person", person);

        NumberList numList = new NumberList(List.of(2, 4, 6, 8, 10));
        evaluator.setVariable("numbers", numList);

        System.out.println(evaluator.evaluate("x + y")); // 15
        System.out.println(evaluator.evaluate("\"Hello \" + name")); // "Hello Alice"
        System.out.println(evaluator.evaluate("person.name + \" lebt in PLZ \" + person.wohnort.plz")); // "Bob lebt in PLZ 12345"
        System.out.println(evaluator.evaluate("numbers.get(4)")); // 10
	}
}
