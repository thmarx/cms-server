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


public class ExpressionParser {
    private final Tokenizer tokenizer;

    public ExpressionParser(String expression) {
        this.tokenizer = new Tokenizer(expression);
    }

    public Object parse() {
        return parseExpression();
    }

    private Object parseExpression() {
        Object left = parseTerm();
        while (tokenizer.hasNext()) {
            tokenizer.consumeWhitespace(); // NEU: Leerzeichen zwischen Operatoren ignorieren
            char op = tokenizer.peek();
            if (op == '+' || op == '-') {
                tokenizer.consume();
                tokenizer.consumeWhitespace();
                Object right = parseTerm();
                left = evaluate(left, right, op);
            } else {
                break;
            }
        }
        return left;
    }

    private Object parseTerm() {
        Object left = parseFactor();
        while (tokenizer.hasNext()) {
            tokenizer.consumeWhitespace(); // NEU
            char op = tokenizer.peek();
            if (op == '*' || op == '/') {
                tokenizer.consume();
                tokenizer.consumeWhitespace(); // NEU
                Object right = parseFactor();
                left = evaluate(left, right, op);
            } else {
                break;
            }
        }
        return left;
    }

    private Object parseFactor() {
        tokenizer.consumeWhitespace(); // NEU
        if (tokenizer.peek() == '(') {
            tokenizer.consume();
            Object result = parseExpression();
            tokenizer.consume(); // ')'
            return result;
        } else if (tokenizer.peek() == '"') {
            return tokenizer.consumeString();
        } else {
            return tokenizer.consumeNumber();
        }
    }

    private Object evaluate(Object left, Object right, char op) {
        if (left instanceof String || right instanceof String) {
            return left.toString() + right.toString();
        }
        double a = ((Number) left).doubleValue();
        double b = ((Number) right).doubleValue();
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> throw new IllegalArgumentException("Ungültiger Operator: " + op);
        };
    }
}


