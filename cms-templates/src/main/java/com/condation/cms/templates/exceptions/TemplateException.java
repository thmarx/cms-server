package com.condation.cms.templates.exceptions;

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

import lombok.Getter;

/**
 * Base exception for template errors with enhanced error reporting.
 * Provides source code snippets and template context for better debugging.
 */
@Getter
public abstract class TemplateException extends RuntimeException {

	private final int line;
	private final int column;
	private final String templateName;
	private final String sourceSnippet;

	public TemplateException(String message, int line, int column) {
		this(message, line, column, null, null);
	}

	public TemplateException(String message, int line, int column, String templateName) {
		this(message, line, column, templateName, null);
	}

	public TemplateException(String message, int line, int column, String templateName, String sourceSnippet) {
		super(message);
		this.line = line;
		this.column = column;
		this.templateName = templateName;
		this.sourceSnippet = sourceSnippet;
	}

	/**
	 * Creates a formatted error message with source context.
	 */
	protected String formatMessage() {
		StringBuilder sb = new StringBuilder();

		// Basic error message
		sb.append("Error: ").append(super.getMessage());

		// Location info
		if (templateName != null) {
			sb.append("\n  in template '").append(templateName).append("'");
		}
		sb.append(" at line ").append(line).append(", column ").append(column);

		// Source snippet if available
		if (sourceSnippet != null && !sourceSnippet.isEmpty()) {
			sb.append("\n\n").append(sourceSnippet);
		}

		return sb.toString();
	}

	@Override
	public String getMessage() {
		return formatMessage();
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}

	/**
	 * Extracts a source code snippet from the template content.
	 *
	 * @param templateContent the full template content
	 * @param errorLine       the line where the error occurred (1-based)
	 * @param contextLines    number of lines to show before and after the error
	 * @return formatted source snippet with line numbers
	 */
	public static String extractSourceSnippet(String templateContent, int errorLine, int contextLines) {
		if (templateContent == null || templateContent.isEmpty()) {
			return null;
		}

		String[] lines = templateContent.split("\n");
		if (errorLine < 1 || errorLine > lines.length) {
			return null;
		}

		StringBuilder snippet = new StringBuilder();
		int startLine = Math.max(1, errorLine - contextLines);
		int endLine = Math.min(lines.length, errorLine + contextLines);

		for (int i = startLine; i <= endLine; i++) {
			String lineContent = i <= lines.length ? lines[i - 1] : "";
			boolean isErrorLine = (i == errorLine);

			// Line number with padding
			String lineNumber = String.format("%4d", i);

			// Add marker for error line
			if (isErrorLine) {
				snippet.append("  → ");
			} else {
				snippet.append("    ");
			}

			snippet.append(lineNumber).append(" | ").append(lineContent);

			if (i < endLine) {
				snippet.append("\n");
			}
		}

		return snippet.toString();
	}

	/**
	 * Creates a column marker string (e.g., "       ^")
	 *
	 * @param column the column position (1-based)
	 * @return a string with spaces and a caret pointing to the column
	 */
	public static String createColumnMarker(int column) {
		if (column < 1) {
			return "";
		}
		return " ".repeat(Math.max(0, column - 1)) + "^";
	}
}
