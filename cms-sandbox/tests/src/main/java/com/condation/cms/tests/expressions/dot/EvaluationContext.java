/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

import java.util.HashMap;
import java.util.Map;

/**
 * Kontext für die Evaluierung
 * Enthält die verfügbaren Daten und Funktionen
 */
public class EvaluationContext {
    private final Map<String, Object> data;
    private final Map<String, ExprFunction> functions;
    
    public EvaluationContext() {
        this.data = new HashMap<>();
        this.functions = new HashMap<>();
    }
    
    public EvaluationContext(Map<String, Object> initialData) {
        this.data = new HashMap<>(initialData);
        this.functions = new HashMap<>();
    }
    
    public void set(String key, Object value) {
        data.put(key, value);
    }
    
    public Object get(String key) {
        if (!data.containsKey(key)) {
            throw new EvaluationException("Variable nicht gefunden: " + key);
        }
        return data.get(key);
    }
    
    public boolean has(String key) {
        return data.containsKey(key);
    }
    
    public void registerFunction(String name, ExprFunction function) {
        functions.put(name, function);
    }
    
    public ExprFunction getFunction(String name) {
        if (!functions.containsKey(name)) {
            throw new EvaluationException("Funktion nicht gefunden: " + name);
        }
        return functions.get(name);
    }
    
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
