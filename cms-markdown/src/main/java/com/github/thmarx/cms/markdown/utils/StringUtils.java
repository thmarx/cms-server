package com.github.thmarx.cms.markdown.utils;

/**
 *
 * @author t.marx
 */
public class StringUtils {

	public static String removeLeadingPipe(String s) {
		return s.replaceAll("^\\|+(?!$)", "");
	}

	public static String removeTrailingPipe(String s) {
		return s.replaceAll("(?!^)\\|+$", "");
	}
}
