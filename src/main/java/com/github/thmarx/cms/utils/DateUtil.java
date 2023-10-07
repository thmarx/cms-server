/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author t.marx
 */
public class DateUtil {
	
	private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
	
	public static void setDateFormat (final String pattern) {
		formatter = DateTimeFormatter.ofPattern(pattern.trim());
	}
	
	public static boolean isDate (final String dateString) {
		try {
			formatter.parse(dateString);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static LocalDate toDate (final String dateString) {
		try {
			return LocalDate.parse(dateString.trim(), formatter);
		} catch (Exception e) {
		}
		return null;
	}
}
