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

import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.exceptions.UnknownTagException;

/**
 * Strict error handler that always throws exceptions.
 * Recommended for development mode to catch errors early.
 */
public class StrictErrorHandler implements ErrorHandler {

	@Override
	public void handleUnknownTag(String tagName, int line, int column, String template) {
		throw new UnknownTagException("Unknown tag: " + tagName, line, column);
	}

	@Override
	public void handleUnknownComponent(String componentName, int line, int column, String template) {
		throw new RenderException("Unknown component: " + componentName, line, column, template);
	}

	@Override
	public void handleUnknownFilter(String filterName, int line, int column, String template) {
		throw new RenderException("Unknown filter: " + filterName, line, column, template);
	}

	@Override
	public void handleRenderError(String message, int line, int column, String template, Throwable cause) {
		if (cause != null) {
			throw new RenderException(message + ": " + cause.getMessage(), line, column, template);
		} else {
			throw new RenderException(message, line, column, template);
		}
	}
}
