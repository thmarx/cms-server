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
public class Tokenizer {

	private final String input;
	private int pos = 0;

	public Tokenizer(String input) {
		this.input = input.trim();
	}

	public boolean hasNext() {
		skipWhitespace();
		return pos < input.length();
	}

	public char peek() {
		skipWhitespace();
		return input.charAt(pos);
	}

	public void consume() {
		pos++;
	}

	public String consumeString() {
		StringBuilder sb = new StringBuilder();
		consume(); // Erstes " überspringen
		while (pos < input.length()) {  // ACHTUNG: Hier KEIN `hasNext()`!
			char c = input.charAt(pos);
			if (c == '"') {
				consume(); // Letztes " überspringen
				break;
			}
			sb.append(c);
			consume();
		}
		return sb.toString();
	}

	public Number consumeNumber() {
		skipWhitespace();
		int start = pos;
		while (hasNext() && (Character.isDigit(peek()) || peek() == '.')) {
			consume();
		}

		return Double.parseDouble(input.substring(start, pos));
	}

	protected void skipWhitespace() {
		while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
			pos++;
		}
	}

	public void consumeWhitespace() {
		while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
			pos++;
		}
	}
}
