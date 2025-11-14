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




import com.google.common.labs.parse.Parser;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ExpressionParser {
    
    // AST Nodes
    interface Expr {}
    
    static class StringLiteral implements Expr {
        final String value;
        StringLiteral(String value) { this.value = value; }
        @Override
        public String toString() { return "\"" + value + "\""; }
    }
    
    static class Identifier implements Expr {
        final List<String> parts;
        Identifier(List<String> parts) { this.parts = parts; }
        @Override
        public String toString() { return String.join(".", parts); }
    }
    
    static class NumberLiteral implements Expr {
        final double value;
        NumberLiteral(double value) { this.value = value; }
        @Override
        public String toString() { return String.valueOf(value); }
    }
    
    static class MapLiteral implements Expr {
        final Map<String, Expr> entries;
        MapLiteral(Map<String, Expr> entries) { this.entries = entries; }
        @Override
        public String toString() { 
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Expr> e : entries.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(e.getKey()).append(": ").append(e.getValue());
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    static class ListLiteral implements Expr {
        final List<Expr> elements;
        ListLiteral(List<Expr> elements) { this.elements = elements; }
        @Override
        public String toString() { 
            return "[" + elements.stream().map(Expr::toString).collect(Collectors.joining(", ")) + "]";
        }
    }
    
    static class MemberAccess implements Expr {
        final Expr object;
        final String member;
        MemberAccess(Expr object, String member) {
            this.object = object;
            this.member = member;
        }
        @Override
        public String toString() { return object + "." + member; }
    }
    
    static class IndexAccess implements Expr {
        final Expr object;
        final Expr index;
        IndexAccess(Expr object, Expr index) {
            this.object = object;
            this.index = index;
        }
        @Override
        public String toString() { return object + "[" + index + "]"; }
    }
    
    static class MethodCall implements Expr {
        final Expr object;
        final List<Expr> params;
        MethodCall(Expr object, List<Expr> params) {
            this.object = object;
            this.params = params;
        }
        @Override
        public String toString() { 
            return object + "(" + params.stream().map(Expr::toString).collect(Collectors.joining(", ")) + ")";
        }
    }
    
    // Parser Definition
    public static Parser<Expr> createParser() {
        // String Literal: "..."
        Parser<String> stringLiteral = Parser.quotedStringWithEscapes('"', Parser.chars(1));
        
        // Number: digits with optional decimal
        Parser<Double> number = Parser.digits()
            .followedBy(Parser.string(".").then(Parser.digits()).optional())
            .source()
            .map(Double::parseDouble);
        
        // Identifier: word characters
        Parser<String> simpleIdent = Parser.word();
        
        // Forward declare for recursive grammar using Parser.define()
        Parser<Expr> exprParser = Parser.define(expr -> {
            
            // Primary literals
            Parser<Expr> stringExpr = stringLiteral.map(StringLiteral::new);
            Parser<Expr> numberExpr = number.map(NumberLiteral::new);
            Parser<Expr> identExpr = simpleIdent.map(s -> new Identifier(Collections.singletonList(s)));
            
            // List: [item, item, item]
            Parser<List<Expr>> listElements = expr
                .zeroOrMoreDelimitedBy(",")
                .between("[", "]");
            Parser<Expr> listExpr = listElements.map(ListLiteral::new);
            
            // Map: {key: value, key2: value} oder {"key": value}
            // Map-Schlüssel können entweder Identifier oder Strings sein
            Parser<String> mapKey = Parser.anyOf(
                stringLiteral,  // "key"
                simpleIdent     // key
            );
            
            Parser<Map<String, Expr>> mapEntries = Parser.sequence(
                mapKey.followedBy(Parser.string(":")),
                expr,
                (key, value) -> new AbstractMap.SimpleEntry<>(key, value)
            )
            .zeroOrMoreDelimitedBy(",")
            .between("{", "}")
            .map(entries -> {
                Map<String, Expr> map = new LinkedHashMap<>();
                for (AbstractMap.SimpleEntry<String, Expr> entry : entries) {
                    map.put(entry.getKey(), entry.getValue());
                }
                return map;
            });
            Parser<Expr> mapExpr = mapEntries.map(MapLiteral::new);
            
            // Primary expression: literals or identifiers
            Parser<Expr> primary = Parser.anyOf(
                stringExpr,
                numberExpr,
                mapExpr,
                listExpr
            ).or(identExpr);
            
            // Postfix operations: .member, ["index"], (params)
            return primary.postfix(
                Parser.anyOf(
                    // .identifier
                    Parser.string(".").then(simpleIdent)
                        .map(member -> (UnaryOperator<Expr>) obj -> 
                            new MemberAccess(obj, member)),
                    // ["string"] or [expr]
                    Parser.string("[")
                        .then(Parser.anyOf(
                            stringLiteral.map(s -> (Expr) new StringLiteral(s)),
                            expr
                        ))
                        .followedBy(Parser.string("]"))
                        .map(index -> (UnaryOperator<Expr>) obj -> 
                            new IndexAccess(obj, index)),
                    // (params)
                    expr.zeroOrMoreDelimitedBy(",")
                        .between("(", ")")
                        .map(params -> (UnaryOperator<Expr>) obj -> 
                            new MethodCall(obj, params))
                )
            );
        });
        
        return exprParser;
    }
    
    // Parsing with whitespace skipping
    public static Expr parse(String input) {
        Parser<Expr> parser = createParser();
        return parser.parseSkipping(Character::isWhitespace, input);
    }
}
