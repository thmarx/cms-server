/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class BooleanUtil {

	private static Pattern booleanPattern = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

	public static boolean isBoolean(final String value) {
		Matcher matcher = booleanPattern.matcher(value.trim());
		return matcher.matches();
	}
	
	public static boolean toBoolean(final String value) {
		return Boolean.parseBoolean(value.trim());
	}
}
