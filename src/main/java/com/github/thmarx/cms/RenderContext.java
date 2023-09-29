/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import java.util.Deque;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public record RenderContext(String uri, Map<String, Deque<String>> queryParameters) {

	public String getQueryParameter(String name, final String defaultValue) {
		if (!queryParameters.containsKey(name)) {
			return defaultValue;
		}
		
		return queryParameters.get(name).getFirst();
	}

	public int getQueryParameterAsInt(String name, final int defaultValue) {
		if (!queryParameters.containsKey(name)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(queryParameters.get(name).getFirst());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}
}
