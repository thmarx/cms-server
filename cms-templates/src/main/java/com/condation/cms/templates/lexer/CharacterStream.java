package com.condation.cms.templates.lexer;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CharacterStream {

    private final String source;

    private int position = 0;

    @Getter
	private int line = 1;
    @Getter
	private int column = 1;


    public boolean hasMore () {
        return position < source.length();
    }

    public char charAtCurrentPosition () {
        return source.charAt(position);
    }

    protected char peek(int offset) {
		return (position + offset < source.length()) ? source.charAt(position + offset) : '\0';
	}

	protected String readWhile(Predicate<Character> condition) {
		StringBuilder result = new StringBuilder();
		while (position < source.length() && condition.test(source.charAt(position))) {
			result.append(source.charAt(position));
			
			advance();
		}
		return result.toString();
	}

	protected String readUntil(BiPredicate<String, Integer> predicate) {
		int start = position;
		while (position < source.length() && predicate.test(source, position)) {
			advance();
		}
		return source.substring(start, position);
	}
	
	protected String readUntil(String delimiter) {
		int index = source.indexOf(delimiter, position);
		if (index == -1) {
			String result = source.substring(position);
			updateLineColumn(position, source.length());
			position = source.length();
			return result;
		} else {
			String result = source.substring(position, index);
			updateLineColumn(position, index);
			position = index;
			return result;
		}
	}
	
	protected String readUntil(String[] delimiters) {
		int earliestIndex = -1;
		for (String delimiter : delimiters) {
			int index = source.indexOf(delimiter, position);
			if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
				earliestIndex = index;
			}
		}

		if (earliestIndex == -1) {
			String result = source.substring(position);
			updateLineColumn(position, source.length());
			position = source.length();
			return result;
		} else {
			String result = source.substring(position, earliestIndex);
			updateLineColumn(position, earliestIndex);
			position = earliestIndex;
			return result;
		}
	}

	private void updateLineColumn(int start, int end) {
		for (int i = start; i < end; i++) {
			if (source.charAt(i) == '\n') {
				line++;
				column = 1;
			} else {
				column++;
			}
		}
	}

	protected void skipWhitespace() {
		while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
			advance();
		}
	}

    protected void skip (int count) {
        position += count;
    }

	protected void advance() {
		if (source.charAt(position) == '\n') {
			line++;
			column = 1;
		} else {
			column++;
		}
		position++;
	}

}
