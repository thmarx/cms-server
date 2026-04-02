package com.condation.cms.templates.error;

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

/**
 * Strategy interface for handling template errors.
 * Allows customization of error handling behavior (strict, lenient, custom).
 */
public interface ErrorHandler {

	/**
	 * Handles an unknown tag error.
	 *
	 * @param tagName  the name of the unknown tag
	 * @param line     the line where the error occurred
	 * @param column   the column where the error occurred
	 * @param template the template name (may be null)
	 */
	void handleUnknownTag(String tagName, int line, int column, String template);

	/**
	 * Handles an unknown component error.
	 *
	 * @param componentName the name of the unknown component
	 * @param line          the line where the error occurred
	 * @param column        the column where the error occurred
	 * @param template      the template name (may be null)
	 */
	void handleUnknownComponent(String componentName, int line, int column, String template);

	/**
	 * Handles an unknown filter error.
	 *
	 * @param filterName the name of the unknown filter
	 * @param line       the line where the error occurred
	 * @param column     the column where the error occurred
	 * @param template   the template name (may be null)
	 */
	void handleUnknownFilter(String filterName, int line, int column, String template);

	/**
	 * Handles a general rendering error.
	 *
	 * @param message  the error message
	 * @param line     the line where the error occurred
	 * @param column   the column where the error occurred
	 * @param template the template name (may be null)
	 * @param cause    the underlying cause (may be null)
	 */
	void handleRenderError(String message, int line, int column, String template, Throwable cause);
}
