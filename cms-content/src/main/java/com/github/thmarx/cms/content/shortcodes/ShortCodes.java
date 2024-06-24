package com.github.thmarx.cms.content.shortcodes;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.model.Parameter;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class ShortCodes {

	public static final Pattern TAG_PARAMS_PATTERN_SHORT = Pattern.compile("\\[{2}(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\p{Blank}*/\\]{2}");
	
	public static final Pattern TAG_PARAMS_PATTERN_LONG = Pattern.compile("\\[{2}(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\]{2}(?<content>.*?)\\[{2}/\\k<tag>\\]{2}");
	
	private final ShortCodeParser.Codes codes;

	public ShortCodes (Map<String, Function<Parameter, String>> codes) {
		this.codes = new ShortCodeParser.Codes();
		this.codes.addAll(codes);
	}
	
	public String replace (final String content) {
		return ShortCodeParser.replace(content, codes);
	}
}
