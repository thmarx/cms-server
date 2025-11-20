package com.condation.cms.templates.renderer;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

public class ExpressionCache {

    private final JexlEngine jexlEngine;
    private final Map<String, JexlExpression> cache = new ConcurrentHashMap<>();

    public ExpressionCache(JexlEngine jexlEngine) {
        this.jexlEngine = jexlEngine;
    }

    public JexlExpression get(String expressionString) {
        return cache.computeIfAbsent(expressionString, jexlEngine::createExpression);
    }

    public void invalidate() {
        cache.clear();
    }
}
