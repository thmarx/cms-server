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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import static com.condation.cms.tests.expressions.dot.ExpressionParser.Expr;
import org.junit.jupiter.api.Disabled;

@DisplayName("ExpressionParser Tests")
public class ExpressionParserTest {

    // ============ String Literals ============
    @Test
    @DisplayName("Parse simple string literal")
    void testSimpleString() {
        ExpressionParser.Expr expr = ExpressionParser.parse("\"hello\"");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.StringLiteral.class);
        assertThat(((ExpressionParser.StringLiteral) expr).value)
            .isEqualTo("hello");
    }

    @Test
    @DisplayName("Parse empty string literal")
    void testEmptyString() {
        Expr expr = ExpressionParser.parse("\"\"");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.StringLiteral.class)
            .extracting(e -> ((ExpressionParser.StringLiteral) e).value)
            .isEqualTo("");
    }

    @Test
    @DisplayName("Parse string with escaped quotes")
    void testStringWithEscapes() {
        Expr expr = ExpressionParser.parse("\"hello\\nworld\"");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.StringLiteral.class);
    }

    // ============ Numbers ============
    @Test
    @DisplayName("Parse integer literal")
    void testInteger() {
        Expr expr = ExpressionParser.parse("42");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.NumberLiteral.class);
        assertThat(((ExpressionParser.NumberLiteral) expr).value)
            .isEqualTo(42.0);
    }

    @Test
    @DisplayName("Parse decimal number")
    void testDecimal() {
        Expr expr = ExpressionParser.parse("3.14");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.NumberLiteral.class);
        assertThat(((ExpressionParser.NumberLiteral) expr).value)
            .isEqualTo(3.14);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "999", "0.1", "99.99"})
    @DisplayName("Parse various numbers")
    void testVariousNumbers(String input) {
        Expr expr = ExpressionParser.parse(input);
        assertThat(expr).isInstanceOf(ExpressionParser.NumberLiteral.class);
    }

    // ============ Identifiers ============
    @Test
    @DisplayName("Parse simple identifier")
    void testSimpleIdentifier() {
        Expr expr = ExpressionParser.parse("myVar");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.Identifier.class);
        assertThat(((ExpressionParser.Identifier) expr).parts)
            .containsExactly("myVar");
    }

    @Test
    @DisplayName("Parse chained identifiers with dots")
    void testChainedIdentifiers() {
        Expr expr = ExpressionParser.parse("parent.child.grandchild");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MemberAccess.class);
    }

    // ============ Lists ============
    @Test
    @DisplayName("Parse empty list")
    void testEmptyList() {
        Expr expr = ExpressionParser.parse("[]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.ListLiteral.class);
        assertThat(((ExpressionParser.ListLiteral) expr).elements)
            .isEmpty();
    }

    @Test
    @DisplayName("Parse list with mixed types")
    void testMixedList() {
        Expr expr = ExpressionParser.parse("[1, \"two\", three]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.ListLiteral.class);
        List<Expr> elements = ((ExpressionParser.ListLiteral) expr).elements;
        assertThat(elements).hasSize(3);
        assertThat(elements.get(0)).isInstanceOf(ExpressionParser.NumberLiteral.class);
        assertThat(elements.get(1)).isInstanceOf(ExpressionParser.StringLiteral.class);
        assertThat(elements.get(2)).isInstanceOf(ExpressionParser.Identifier.class);
    }

    @Test
    @DisplayName("Parse nested lists")
    void testNestedLists() {
        Expr expr = ExpressionParser.parse("[[1, 2], [3, 4]]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.ListLiteral.class);
        List<Expr> outer = ((ExpressionParser.ListLiteral) expr).elements;
        assertThat(outer).allMatch(e -> e instanceof ExpressionParser.ListLiteral);
    }

    // ============ Maps ============
    @Test
    @DisplayName("Parse empty map")
    void testEmptyMap() {
        Expr expr = ExpressionParser.parse("{}");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MapLiteral.class);
        assertThat(((ExpressionParser.MapLiteral) expr).entries)
            .isEmpty();
    }

    @Test
    @DisplayName("Parse simple map")
    void testSimpleMap() {
        Expr expr = ExpressionParser.parse("{name: \"John\", age: 30}");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MapLiteral.class);
        Map<String, Expr> entries = ((ExpressionParser.MapLiteral) expr).entries;
        assertThat(entries)
            .hasSize(2)
            .containsKeys("name", "age");
    }

    @Test
    @DisplayName("Parse map with mixed value types")
    void testMapWithMixedTypes() {
        Expr expr = ExpressionParser.parse("{x: 5, y: \"test\", z: zVar}");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MapLiteral.class);
        Map<String, Expr> entries = ((ExpressionParser.MapLiteral) expr).entries;
        assertThat(entries).hasSize(3);
        assertThat(entries.get("x")).isInstanceOf(ExpressionParser.NumberLiteral.class);
        assertThat(entries.get("y")).isInstanceOf(ExpressionParser.StringLiteral.class);
        assertThat(entries.get("z")).isInstanceOf(ExpressionParser.Identifier.class);
    }

    // ============ Member Access ============
    @Test
    @DisplayName("Parse simple member access")
    void testSimpleMemberAccess() {
        Expr expr = ExpressionParser.parse("obj.prop");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MemberAccess.class);
        ExpressionParser.MemberAccess access = (ExpressionParser.MemberAccess) expr;
        assertThat(access.member).isEqualTo("prop");
        assertThat(access.object).isInstanceOf(ExpressionParser.Identifier.class);
    }

    @Test
    @DisplayName("Parse deep member access")
    void testDeepMemberAccess() {
        Expr expr = ExpressionParser.parse("obj.level1.level2.level3");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MemberAccess.class);
    }

    // ============ Index Access ============
    @Test
    @DisplayName("Parse index access with string key")
    void testIndexAccessString() {
        Expr expr = ExpressionParser.parse("obj[\"key\"]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.IndexAccess.class);
        ExpressionParser.IndexAccess access = (ExpressionParser.IndexAccess) expr;
        assertThat(access.index)
            .isInstanceOf(ExpressionParser.StringLiteral.class);
    }

    @Test
    @DisplayName("Parse index access with number")
    void testIndexAccessNumber() {
        Expr expr = ExpressionParser.parse("arr[0]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.IndexAccess.class);
        ExpressionParser.IndexAccess access = (ExpressionParser.IndexAccess) expr;
        assertThat(access.index)
            .isInstanceOf(ExpressionParser.NumberLiteral.class);
    }

    @Test
    @DisplayName("Parse index access with identifier")
    void testIndexAccessIdentifier() {
        Expr expr = ExpressionParser.parse("arr[index]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.IndexAccess.class);
        ExpressionParser.IndexAccess access = (ExpressionParser.IndexAccess) expr;
        assertThat(access.index)
            .isInstanceOf(ExpressionParser.Identifier.class);
    }

    // ============ Method Calls ============
    @Test
    @DisplayName("Parse method call without parameters")
    void testMethodCallNoParams() {
        Expr expr = ExpressionParser.parse("obj.method()");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).isEmpty();
        assertThat(call.object)
            .isInstanceOf(ExpressionParser.MemberAccess.class);
    }

    @Test
    @DisplayName("Parse method call with single parameter")
    void testMethodCallSingleParam() {
        Expr expr = ExpressionParser.parse("obj.method(42)");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).hasSize(1);
        assertThat(call.params.get(0))
            .isInstanceOf(ExpressionParser.NumberLiteral.class);
    }

    @Test
    @DisplayName("Parse method call with multiple parameters")
    void testMethodCallMultipleParams() {
        Expr expr = ExpressionParser.parse("func(1, \"two\", three)");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).hasSize(3);
    }

    // ============ Complex Cases ============
    @Test
    @DisplayName("Parse method call on member with complex parameters")
    void testComplexMethodCall() {
        Expr expr = ExpressionParser.parse("parent.child.method({\"name\": \"John\", id: 123})");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).hasSize(1);
        assertThat(call.params.get(0))
            .isInstanceOf(ExpressionParser.MapLiteral.class);
        
        Map<String, Expr> mapEntries = ((ExpressionParser.MapLiteral) call.params.get(0)).entries;
        assertThat(mapEntries)
            .hasSize(2)
            .containsKeys("name", "id");
    }

    @Test
    @DisplayName("Parse method call with list parameter containing map")
    void testMethodCallWithListAndMap() {
        Expr expr = ExpressionParser.parse("api.send([{type: \"user\", id: 5}, {type: \"admin\", id: 1}])");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).hasSize(1);
        List<Expr> listElements = ((ExpressionParser.ListLiteral) call.params.get(0)).elements;
        assertThat(listElements)
            .hasSize(2)
            .allMatch(e -> e instanceof ExpressionParser.MapLiteral);
    }

    @Test
    @DisplayName("Parse deeply nested access and calls")
    void testDeepNesting() {
        Expr expr = ExpressionParser.parse("root.child[0].method(param).result[\"key\"]");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.IndexAccess.class);
    }

    @Test
    @DisplayName("Parse map access with method")
    void testMapAccessWithMethod() {
        Expr expr = ExpressionParser.parse("obj[\"method\"](1, 2, 3)");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
        ExpressionParser.MethodCall call = (ExpressionParser.MethodCall) expr;
        assertThat(call.params).hasSize(3);
    }

    @Test
    @DisplayName("Parse identifier access followed by method call on result")
    void testChainedMethodCalls() {
        Expr expr = ExpressionParser.parse("service.getData(filter).process().result");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MemberAccess.class);
    }

    @Test
	@Disabled
    @DisplayName("Parse complex real-world example")
    void testRealWorldExample() {
        Expr expr = ExpressionParser.parse(
            "db.users.find({status: \"active\", age: 25}).map(user -> user.profile[0].name).sort()");
        assertThat(expr).isNotNull();
    }

    // ============ Error Cases ============
    @Test
    @DisplayName("Handle parsing error gracefully")
    void testParsingError() {
        assertThatThrownBy(() -> ExpressionParser.parse("obj."))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Handle mismatched brackets")
    void testMismatchedBrackets() {
        assertThatThrownBy(() -> ExpressionParser.parse("[1, 2, 3"))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Handle unclosed string")
    void testUnclosedString() {
        assertThatThrownBy(() -> ExpressionParser.parse("\"unclosed"))
            .isInstanceOf(Exception.class);
    }

    // ============ Whitespace Handling ============
    @Test
    @DisplayName("Parse with excessive whitespace")
    void testWhitespaceHandling() {
        Expr expr = ExpressionParser.parse("  obj  .  method  (  1  ,  2  )  ");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.MethodCall.class);
    }

    @Test
    @DisplayName("Parse with whitespace in string preserved")
    void testWhitespaceInString() {
        Expr expr = ExpressionParser.parse("\"  hello  world  \"");
        assertThat(expr)
            .isInstanceOf(ExpressionParser.StringLiteral.class);
        assertThat(((ExpressionParser.StringLiteral) expr).value)
            .contains("hello");
    }
}
