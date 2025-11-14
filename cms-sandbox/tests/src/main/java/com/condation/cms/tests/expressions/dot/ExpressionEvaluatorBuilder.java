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

import java.util.List;
import java.util.Map;

/**
 * Builder für die komfortable Erstellung von Evaluatoren
 */
public class ExpressionEvaluatorBuilder {
    private final EvaluationContext context;
    
    public ExpressionEvaluatorBuilder() {
        this.context = new EvaluationContext();
    }
    
    public ExpressionEvaluatorBuilder withVariable(String name, Object value) {
        context.set(name, value);
        return this;
    }
    
    public ExpressionEvaluatorBuilder withData(Map<String, Object> data) {
        data.forEach(context::set);
        return this;
    }
    
    public ExpressionEvaluatorBuilder withFunction(String name, ExprFunction function) {
        context.registerFunction(name, function);
        return this;
    }
    
    public ExpressionEvaluatorBuilder withStandardFunctions() {
        // Length/Size
        context.registerFunction("length", params -> {
            if (params.isEmpty()) throw new EvaluationException("length erfordert 1 Parameter");
            Object obj = params.get(0);
            if (obj instanceof String) return ((String) obj).length();
            if (obj instanceof List) return ((List<?>) obj).size();
            if (obj instanceof Map) return ((Map<?, ?>) obj).size();
            throw new EvaluationException("length unterstützt diesen Typ nicht");
        });
        
        // Uppercase
        context.registerFunction("upper", params -> {
            if (params.isEmpty()) throw new EvaluationException("upper erfordert 1 Parameter");
            return params.get(0).toString().toUpperCase();
        });
        
        // Lowercase
        context.registerFunction("lower", params -> {
            if (params.isEmpty()) throw new EvaluationException("lower erfordert 1 Parameter");
            return params.get(0).toString().toLowerCase();
        });
        
        // Concat
        context.registerFunction("concat", params -> {
            StringBuilder sb = new StringBuilder();
            for (Object param : params) {
                sb.append(param);
            }
            return sb.toString();
        });
        
        // Sum (für Listen)
        context.registerFunction("sum", params -> {
            if (params.isEmpty()) throw new EvaluationException("sum erfordert 1 Parameter");
            Object obj = params.get(0);
            if (!(obj instanceof List)) throw new EvaluationException("sum erfordert eine Liste");
            double sum = 0;
            for (Object item : (List<?>) obj) {
                if (item instanceof Number) {
                    sum += ((Number) item).doubleValue();
                }
            }
            return sum;
        });
        
        return this;
    }
    
    public ExpressionEvaluator build() {
        return new ExpressionEvaluator(context);
    }
}
