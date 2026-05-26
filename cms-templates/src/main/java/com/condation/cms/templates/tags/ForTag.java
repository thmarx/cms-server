package com.condation.cms.templates.tags;

/*-
 * #%L
 * CMS Templates
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
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.exceptions.TagException;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ForTag implements Tag {

    @Override
    public String getTagName() {
        return "for";
    }

    @Override
    public Set<String> getCloseTagNames() {
        return Set.of("endfor", "/for");
    }

    @Override
    public void render(TagNode node, Renderer.Context context, Writer writer) {

        ForDefinition def = parseForLoop(node);

        Iterable<?> iterable = resolveIterable(def.collection(), context, node);

        int index = 0;

        for (Object item : iterable) {

            Map<String, Object> loopScope = new HashMap<>();
            loopScope.put("loop", new Loop(index++));

            applyIterationVariables(loopScope, def, item);

            context.scopes().pushScope(loopScope);

            try {
                for (var child : node.getChildren()) {
                    context.renderer().render(child, context, writer);
                }
            } catch (IOException e) {
                throw new RenderException(e.getMessage(), node.getLine(), node.getColumn());
            } finally {
                context.scopes().popScope();
            }
        }
    }

    /**
     * Converts supported structures into iterable form
     */
    private Iterable<?> resolveIterable(
            String expression,
            Renderer.Context context,
            TagNode node) {

        expression = expression.trim();

        // optionale Klammern entfernen
        if (expression.startsWith("(") && expression.endsWith(")")) {
            expression = expression.substring(1, expression.length() - 1).trim();
        }

        // Range-Syntax erkennen
        if (expression.contains("..")) {
            return resolveRange(expression, context, node);
        }

        Object value = context.engine()
                .createExpression(expression)
                .evaluate(context.createEngineContext());

        if (value == null) {
            throw new TagException("Iterable is null", node.getLine(), node.getColumn());
        }

        if (value instanceof Map<?, ?> map) {
            return map.entrySet();
        }

        if (value instanceof Iterable<?> iterable) {
            return iterable;
        }

        throw new TagException(
                "Unsupported iterable type: " + value.getClass().getName(),
                node.getLine(),
                node.getColumn()
        );
    }

    private Iterable<Integer> resolveRange(
            String expression,
            Renderer.Context context,
            TagNode node) {

        String[] parts = expression.split("\\.\\.");

        if (parts.length != 2) {
            throw new TagException(
                    "Invalid range expression: " + expression,
                    node.getLine(),
                    node.getColumn()
            );
        }

        Object startValue = context.engine()
                .createExpression(parts[0].trim())
                .evaluate(context.createEngineContext());

        Object endValue = context.engine()
                .createExpression(parts[1].trim())
                .evaluate(context.createEngineContext());

        int start = toInt(startValue, node);
        int end = toInt(endValue, node);

        return new Range(start, end);
    }

    private int toInt(Object value, TagNode node) {

        if (value instanceof Number number) {
            return number.intValue();
        }

        throw new TagException(
                "Range boundaries must be numeric but got: " + value,
                node.getLine(),
                node.getColumn()
        );
    }

    /**
     * Handles variable binding inside loop scope
     */
    private void applyIterationVariables(Map<String, Object> scope, ForDefinition def, Object item) {

        // Map iteration: "key, value in map"
        if (item instanceof Map.Entry<?, ?> entry && def.isMapEntry()) {

            scope.put(def.variable1(), entry.getKey());
            scope.put(def.variable2(), entry.getValue());

        } else {
            scope.put(def.variable1(), item);
        }
    }

    /**
     * Parses: - "item in list" - "key, value in map"
     */
    private ForDefinition parseForLoop(TagNode node) {

        String expr = node.getCondition();

        if (!expr.contains(" in ")) {
            throw new TagException("Invalid for-loop syntax: " + expr, node.getLine(), node.getColumn());
        }

        String[] parts = expr.split(" in ");

        if (parts.length != 2) {
            throw new TagException("Invalid for-loop syntax: " + expr, node.getLine(), node.getColumn());
        }

        String varPart = parts[0].trim();
        String collection = parts[1].trim();

        if (varPart.contains(",")) {
            String[] vars = varPart.split(",");
            if (vars.length != 2) {
                throw new TagException("Invalid map iteration syntax: " + expr, node.getLine(), node.getColumn());
            }

            return new ForDefinition(
                    collection,
                    vars[0].trim(),
                    vars[1].trim()
            );
        }

        return new ForDefinition(collection, varPart.trim(), null);
    }

    @RequiredArgsConstructor
    public static class Loop {

        @Getter
        public final int index;
    }

    private static record ForDefinition(
            String collection,
            String variable1,
            String variable2
            ) {

        boolean isMapEntry() {
            return variable2 != null;
        }
    }
}