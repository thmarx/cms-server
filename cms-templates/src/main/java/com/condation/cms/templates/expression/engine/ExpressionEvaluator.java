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

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

public class ExpressionEvaluator {
    private final Map<String, Object> variables = new HashMap<>();

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object evaluate(String expression) throws ExpressionException {
        try {
            expression = resolveDotNotation(expression);
            return new ExpressionParser(expression).parse();
        } catch (Exception e) {
            throw new ExpressionException("Fehler bei der Auswertung: " + expression, e);
        }
    }

    private String resolveDotNotation(String expression) throws Exception {
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)(\\.[a-zA-Z_][a-zA-Z0-9_]*(\\(.*?\\))?)*");
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String fullPath = matcher.group();
            Object value = resolveObjectPath(fullPath);
            if (value != null) {
                if (value instanceof String) {
                    expression = expression.replace(fullPath, "\"" + value + "\"");
                } else {
                    expression = expression.replace(fullPath, value.toString());
                }
            }
        }
        return expression;
    }

    private Object resolveObjectPath(String fullPath) throws Exception {
        String[] parts = fullPath.split("\\.");
        Object currentObject = variables.get(parts[0]);

        if (currentObject == null) return null;

        for (int i = 1; i < parts.length; i++) {
            String fieldOrMethod = parts[i];

            if (fieldOrMethod.contains("(")) {
                currentObject = invokeMethod(currentObject, fieldOrMethod);
            } else {
                currentObject = getFieldValue(currentObject, fieldOrMethod);
            }

            if (currentObject == null) return null;
        }
        return currentObject;
    }

    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj.getClass();

        // 1️⃣ `getField()` Methode zuerst prüfen
        try {
            Method getterMethod = clazz.getMethod("get" + capitalize(fieldName));
            return getterMethod.invoke(obj);
        } catch (NoSuchMethodException ignored) {}

        // 2️⃣ Direkt auf Feld zugreifen
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException ignored) {}

        // 3️⃣ Für `records`, die keine Felder haben, aber accessor-Methoden
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(fieldName) && method.getParameterCount() == 0) {
                return method.invoke(obj);
            }
        }

        return null;
    }

    private Object invokeMethod(Object obj, String methodCall) throws Exception {
        Pattern methodPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\((.*?)\\)");
        Matcher matcher = methodPattern.matcher(methodCall);
        
        if (!matcher.matches()) return null;

        String methodName = matcher.group(1);
        String argStr = matcher.group(2);
        String[] argParts = argStr.isEmpty() ? new String[0] : argStr.split(",");
        
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == argParts.length) {
                Object[] convertedArgs = new Object[argParts.length];
                Class<?>[] paramTypes = method.getParameterTypes();

                for (int i = 0; i < argParts.length; i++) {
                    convertedArgs[i] = convertArgument(argParts[i].trim(), paramTypes[i]);
                }

                return method.invoke(obj, convertedArgs);
            }
        }
        return null;
    }

    private Object convertArgument(String arg, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(arg);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(arg);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(arg);
        }
        if (targetType == String.class) {
            return arg.replace("\"", "");
        }
        return null;
    }

    private String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
