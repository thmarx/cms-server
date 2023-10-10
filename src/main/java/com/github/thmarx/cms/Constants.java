/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public abstract class Constants {
	
	public static final Pattern SECTION_PATTERN = Pattern.compile("\\w+[a-zA-Z0-9-]*\\.(?<section>[a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.md");
	
	public static final Function<String, Pattern> SECTION_OF_PATTERN = (fileName) -> {
		return Pattern.compile("%s\\.([a-zA-Z0-9]+[a-zA-Z0-9-]*)\\.md".formatted(fileName));
	};
}
