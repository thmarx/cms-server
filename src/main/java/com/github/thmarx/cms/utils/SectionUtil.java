/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.utils;

import com.github.thmarx.cms.Constants;

/**
 *
 * @author t.marx
 */
public class SectionUtil {
	
	public static boolean isOrderedSection (final String name) {
		return Constants.SECTION_ORDERED_PATTERN.matcher(name).matches();
	}
	
	public static String getSectionName (final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		} else {
			var matcher = Constants.SECTION_PATTERN.matcher(name);
			matcher.matches();
			return matcher.group("section");
		}
	}
	public static int getSectionIndex (final String name) {
		if (isOrderedSection(name)) {
			var matcher = Constants.SECTION_ORDERED_PATTERN.matcher(name);
			matcher.matches();
			return Integer.parseInt(matcher.group("index"));
		} else {
			return Constants.DEFAULT_SECTION_ORDERED_INDEX;
		}
	}
}
