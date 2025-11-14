package com.condation.cms.tests.expressions.dot;

/*-
 * #%L
 * tests
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExpressionEvaluator Tests")
public class ExpressionEvaluatorTest {
    
    private ExpressionEvaluatorBuilder builder;
    
    @BeforeEach
    void setup() {
        builder = new ExpressionEvaluatorBuilder();
    }
    
    // ============ Literal Evaluation ============
    @Test
    @DisplayName("Evaluate string literal")
    void testStringLiteral() {
        ExpressionParser.Expr expr = ExpressionParser.parse("\"hello\"");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("hello");
    }
    
    @Test
    @DisplayName("Evaluate number literal")
    void testNumberLiteral() {
        ExpressionParser.Expr expr = ExpressionParser.parse("42");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(42.0);
    }
    
    // ============ Variable Access ============
    @Test
    @DisplayName("Evaluate variable access")
    void testVariableAccess() {
        ExpressionParser.Expr expr = ExpressionParser.parse("name");
        ExpressionEvaluator evaluator = builder
            .withVariable("name", "John")
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("John");
    }
    
    @Test
    @DisplayName("Throw error for undefined variable")
    void testUndefinedVariable() {
        ExpressionParser.Expr expr = ExpressionParser.parse("unknown");
        ExpressionEvaluator evaluator = builder.build();
        
        assertThatThrownBy(() -> evaluator.evaluate(expr))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Variable nicht gefunden");
    }
    
    // ============ List Evaluation ============
    @Test
    @DisplayName("Evaluate empty list")
    void testEmptyList() {
        ExpressionParser.Expr expr = ExpressionParser.parse("[]");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<?>) result).isEmpty();
    }
    
    @Test
    @DisplayName("Evaluate list with literals")
    void testListWithLiterals() {
        ExpressionParser.Expr expr = ExpressionParser.parse("[1, 2, 3]");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result)
            .isInstanceOf(List.class)
            .asList()
            .containsExactly(1.0, 2.0, 3.0);
    }
    
    @Test
    @DisplayName("Evaluate list with mixed types")
    void testListWithMixedTypes() {
        ExpressionParser.Expr expr = ExpressionParser.parse("[1, \"two\", 3.0]");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result)
            .isInstanceOf(List.class)
            .asList()
            .containsExactly(1.0, "two", 3.0);
    }
    
    @Test
    @DisplayName("Evaluate list with variable reference")
    void testListWithVariable() {
        ExpressionParser.Expr expr = ExpressionParser.parse("[1, count, 3]");
        ExpressionEvaluator evaluator = builder
            .withVariable("count", 2)
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result)
            .isInstanceOf(List.class)
            .asList()
            .containsExactly(1.0, 2, 3.0);
    }
    
    // ============ Map Evaluation ============
    @Test
    @DisplayName("Evaluate empty map")
    void testEmptyMap() {
        ExpressionParser.Expr expr = ExpressionParser.parse("{}");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) result).isEmpty();
    }
    
    @Test
    @DisplayName("Evaluate map with identifier keys")
    void testMapWithIdentifierKeys() {
        ExpressionParser.Expr expr = ExpressionParser.parse("{name: \"John\", age: 30}");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result)
            .isInstanceOf(Map.class);
        Map<Object, Object> map = (Map<Object, Object>) result;
        assertThat(map)
            .hasSize(2)
            .containsEntry("name", "John")
            .containsEntry("age", 30.0);
    }
    
    @Test
    @DisplayName("Evaluate map with string keys")
    void testMapWithStringKeys() {
        ExpressionParser.Expr expr = ExpressionParser.parse("{\"name\": \"Alice\", \"id\": 5}");
        ExpressionEvaluator evaluator = builder.build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result)
            .isInstanceOf(Map.class);
        Map<Object, Object> map = (Map<Object, Object>) result;
        assertThat(map)
            .containsEntry("name", "Alice")
            .containsEntry("id", 5.0);
    }
    
    @Test
    @DisplayName("Evaluate map with variables")
    void testMapWithVariables() {
        ExpressionParser.Expr expr = ExpressionParser.parse("{status: status, count: total}");
        ExpressionEvaluator evaluator = builder
            .withVariable("status", "active")
            .withVariable("total", 100)
            .build();
        
        Object result = evaluator.evaluate(expr);
        Map<Object, Object> map = (Map<Object, Object>) result;
        assertThat(map)
            .containsEntry("status", "active")
            .containsEntry("count", 100);
    }
    
    // ============ Member Access ============
    @Test
    @DisplayName("Evaluate member access on map")
    void testMemberAccessMap() {
        ExpressionParser.Expr expr = ExpressionParser.parse("user.name");
        ExpressionEvaluator evaluator = builder
            .withVariable("user", Map.of("name", "Bob", "age", 25))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("Bob");
    }
    
    @Test
    @DisplayName("Throw error for missing member")
    void testMissingMember() {
        ExpressionParser.Expr expr = ExpressionParser.parse("user.email");
        ExpressionEvaluator evaluator = builder
            .withVariable("user", Map.of("name", "Bob"))
            .build();
        
        assertThatThrownBy(() -> evaluator.evaluate(expr))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Member nicht gefunden");
    }
    
    // ============ Index Access ============
    @Test
    @DisplayName("Evaluate index access on list")
    void testIndexAccessList() {
        ExpressionParser.Expr expr = ExpressionParser.parse("items[0]");
        ExpressionEvaluator evaluator = builder
            .withVariable("items", Arrays.asList("first", "second", "third"))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("first");
    }
    
    @Test
    @DisplayName("Evaluate index access on map")
    void testIndexAccessMap() {
        ExpressionParser.Expr expr = ExpressionParser.parse("config[\"timeout\"]");
        ExpressionEvaluator evaluator = builder
            .withVariable("config", Map.of("timeout", 3000, "retries", 5))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(3000);
    }
    
    @Test
    @DisplayName("Throw error for index out of bounds")
    void testIndexOutOfBounds() {
        ExpressionParser.Expr expr = ExpressionParser.parse("items[10]");
        ExpressionEvaluator evaluator = builder
            .withVariable("items", Arrays.asList("a", "b"))
            .build();
        
        assertThatThrownBy(() -> evaluator.evaluate(expr))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Index out of bounds");
    }
    
    // ============ Method Calls with Standard Functions ============
    @Test
    @DisplayName("Call length function on string")
    void testLengthFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("length(\"hello\")");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Call length function on list")
    void testLengthFunctionList() {
        ExpressionParser.Expr expr = ExpressionParser.parse("length(items)");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withVariable("items", Arrays.asList(1, 2, 3))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Call upper function")
    void testUpperFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("upper(name)");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withVariable("name", "john")
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("JOHN");
    }
    
    @Test
    @DisplayName("Call lower function")
    void testLowerFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("lower(\"HELLO\")");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("hello");
    }
    
    @Test
    @DisplayName("Call concat function")
    void testConcatFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("concat(first, \"-\", last)");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withVariable("first", "John")
            .withVariable("last", "Doe")
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("John-Doe");
    }
    
    @Test
    @DisplayName("Call sum function")
    void testSumFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("sum(numbers)");
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withVariable("numbers", Arrays.asList(1, 2, 3, 4, 5))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(15.0);
    }
    
    // ============ Custom Functions ============
    @Test
    @DisplayName("Call custom function")
    void testCustomFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("greet(name)");
        ExpressionEvaluator evaluator = builder
            .withFunction("greet", params -> "Hello, " + params.get(0) + "!")
            .withVariable("name", "World")
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("Hello, World!");
    }
    
    @Test
    @DisplayName("Call custom function with multiple parameters")
    void testCustomFunctionMultipleParams() {
        ExpressionParser.Expr expr = ExpressionParser.parse("add(a, b)");
        ExpressionEvaluator evaluator = builder
            .withFunction("add", params -> {
                double a = ((Number) params.get(0)).doubleValue();
                double b = ((Number) params.get(1)).doubleValue();
                return a + b;
            })
            .withVariable("a", 5)
            .withVariable("b", 3)
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(8.0);
    }
    
    // ============ Complex Expressions ============
    @Test
    @DisplayName("Evaluate complex nested expression")
    void testComplexExpression() {
        ExpressionParser.Expr expr = ExpressionParser.parse("user.profile[0].name");
        
        Map<String, Object> profile = Map.of(
            "name", "Alice",
            "email", "alice@example.com"
        );
        
        ExpressionEvaluator evaluator = builder
            .withVariable("user", Map.of("profile", Arrays.asList(profile)))
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("Alice");
    }
    
    @Test
    @DisplayName("Evaluate function call with map parameter")
    void testFunctionWithMapParam() {
        ExpressionParser.Expr expr = ExpressionParser.parse("process({type: \"user\", id: 123})");
        
        ExpressionEvaluator evaluator = builder
            .withFunction("process", params -> {
                Map<?, ?> data = (Map<?, ?>) params.get(0);
                return data.get("type") + "_" + data.get("id");
            })
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("user_123.0");
    }
    
    @Test
    @DisplayName("Evaluate function call with list parameter")
    void testFunctionWithListParam() {
        ExpressionParser.Expr expr = ExpressionParser.parse("filterLength([1, 2, 3, 4, 5])");
        
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withFunction("filterLength", params -> {
                List<?> items = (List<?>) params.get(0);
                return items.size() > 3;
            })
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo(true);
    }
    
    @Test
    @DisplayName("Evaluate chained method calls")
    void testChainedCalls() {
        ExpressionParser.Expr expr = ExpressionParser.parse("upper(lower(\"HELLO\"))");
        
        ExpressionEvaluator evaluator = builder
            .withStandardFunctions()
            .withFunction("upper", params -> params.get(0).toString().toUpperCase())
            .withFunction("lower", params -> params.get(0).toString().toLowerCase())
            .build();
        
        Object result = evaluator.evaluate(expr);
        assertThat(result).isEqualTo("HELLO");
    }
    
    // ============ Error Handling ============
    @Test
    @DisplayName("Throw error for undefined function")
    void testUndefinedFunction() {
        ExpressionParser.Expr expr = ExpressionParser.parse("unknownFunc()");
        ExpressionEvaluator evaluator = builder.build();
        
        assertThatThrownBy(() -> evaluator.evaluate(expr))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Funktion nicht registriert");
    }
    
    @Test
    @DisplayName("Handle function execution exception")
    void testFunctionException() {
        ExpressionParser.Expr expr = ExpressionParser.parse("divide(10, 0)");
        
        ExpressionEvaluator evaluator = builder
            .withFunction("divide", params -> {
                double a = ((Number) params.get(0)).doubleValue();
                double b = ((Number) params.get(1)).doubleValue();
                if (b == 0) throw new IllegalArgumentException("Division by zero");
                return a / b;
            })
            .build();
        
        assertThatThrownBy(() -> evaluator.evaluate(expr))
            .isInstanceOf(EvaluationException.class)
            .hasMessageContaining("Fehler beim Aufrufen");
    }
}
