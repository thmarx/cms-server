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

import java.util.*;


/**
 * Function Interface für aufgerufene Funktionen
 * Input: Liste der evaluierten Parameter
 * Output: Das Ergebnis der Funktion
 */
@FunctionalInterface
interface ExprFunction {
    Object invoke(List<Object> params) throws Exception;
}



/**
 * Hauptklasse für die Evaluierung von Expressions
 */
public class ExpressionEvaluator {
    
    private final EvaluationContext context;
    
    public ExpressionEvaluator(EvaluationContext context) {
        this.context = context;
    }
    
    /**
     * Evaluiert eine Expression im Kontext und gibt das Ergebnis zurück
     */
    public Object evaluate(ExpressionParser.Expr expr) {
        return evaluateExpr(expr);
    }
    
    private Object evaluateExpr(ExpressionParser.Expr expr) {
        if (expr instanceof ExpressionParser.StringLiteral) {
            return ((ExpressionParser.StringLiteral) expr).value;
        }
        
        if (expr instanceof ExpressionParser.NumberLiteral) {
            return ((ExpressionParser.NumberLiteral) expr).value;
        }
        
        if (expr instanceof ExpressionParser.Identifier) {
            return evaluateIdentifier((ExpressionParser.Identifier) expr);
        }
        
        if (expr instanceof ExpressionParser.ListLiteral) {
            return evaluateList((ExpressionParser.ListLiteral) expr);
        }
        
        if (expr instanceof ExpressionParser.MapLiteral) {
            return evaluateMap((ExpressionParser.MapLiteral) expr);
        }
        
        if (expr instanceof ExpressionParser.MemberAccess) {
            return evaluateMemberAccess((ExpressionParser.MemberAccess) expr);
        }
        
        if (expr instanceof ExpressionParser.IndexAccess) {
            return evaluateIndexAccess((ExpressionParser.IndexAccess) expr);
        }
        
        if (expr instanceof ExpressionParser.MethodCall) {
            return evaluateMethodCall((ExpressionParser.MethodCall) expr);
        }
        
        throw new EvaluationException("Unbekannter Expression-Typ: " + expr.getClass().getSimpleName());
    }
    
    private Object evaluateIdentifier(ExpressionParser.Identifier ident) {
        String name = ident.parts.get(0);
        if (!context.has(name)) {
            throw new EvaluationException("Variable nicht gefunden: " + name);
        }
        return context.get(name);
    }
    
    private Object evaluateList(ExpressionParser.ListLiteral list) {
        List<Object> result = new ArrayList<>();
        for (ExpressionParser.Expr elem : list.elements) {
            result.add(evaluateExpr(elem));
        }
        return result;
    }
    
    private Object evaluateMap(ExpressionParser.MapLiteral map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ExpressionParser.Expr> entry : map.entries.entrySet()) {
            result.put(entry.getKey(), evaluateExpr(entry.getValue()));
        }
        return result;
    }
    
    private Object evaluateMemberAccess(ExpressionParser.MemberAccess access) {
        Object obj = evaluateExpr(access.object);
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            if (!map.containsKey(access.member)) {
                throw new EvaluationException("Member nicht gefunden: " + access.member);
            }
            return map.get(access.member);
        }
        
        throw new EvaluationException("Kann nicht auf Member zugreifen bei Typ: " + obj.getClass().getSimpleName());
    }
    
    private Object evaluateIndexAccess(ExpressionParser.IndexAccess access) {
        Object obj = evaluateExpr(access.object);
        Object index = evaluateExpr(access.index);
        
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            int idx = toInt(index);
            if (idx < 0 || idx >= list.size()) {
                throw new EvaluationException("Index out of bounds: " + idx);
            }
            return list.get(idx);
        }
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            String key = index.toString();
            if (!map.containsKey(key)) {
                throw new EvaluationException("Key nicht gefunden: " + key);
            }
            return map.get(key);
        }
        
        throw new EvaluationException("Index access nicht unterstützt für Typ: " + obj.getClass().getSimpleName());
    }
    
    /**
     * BUGFIX: Funktionsnamen ZUERST extrahieren, nicht call.object evaluieren!
     * Das war der Fehler - es versuchte "length" als Variable zu suchen.
     */
    private Object evaluateMethodCall(ExpressionParser.MethodCall call) {
        // Ermittle den Funktionsnamen ZUERST
        String functionName = extractFunctionName(call.object);
        
        // Evaluiere alle Parameter
        List<Object> params = new ArrayList<>();
        for (ExpressionParser.Expr param : call.params) {
            params.add(evaluateExpr(param));
        }
        
        // Rufe die Funktion auf
        try {
            if (context.hasFunction(functionName)) {
                ExprFunction func = context.getFunction(functionName);
                return func.invoke(params);
            }
            
            throw new EvaluationException("Funktion nicht registriert: " + functionName);
        } catch (Exception e) {
            if (e instanceof EvaluationException) {
                throw (EvaluationException) e;
            }
            throw new EvaluationException("Fehler beim Aufrufen von Funktion: " + functionName, e);
        }
    }
    
    private String extractFunctionName(ExpressionParser.Expr expr) {
        if (expr instanceof ExpressionParser.Identifier) {
            return ((ExpressionParser.Identifier) expr).parts.get(0);
        }
        
        if (expr instanceof ExpressionParser.MemberAccess) {
            ExpressionParser.MemberAccess access = (ExpressionParser.MemberAccess) expr;
            return access.member;
        }
        
        throw new EvaluationException("Kann Funktionsnamen nicht ermitteln");
    }
    
    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new EvaluationException("Kann nicht in Integer konvertieren: " + value);
    }
}
