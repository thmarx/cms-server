package com.condation.cms.templates.error;

/*-
 * #%L
 * CMS Templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lenient error handler that logs warnings instead of throwing exceptions.
 * Recommended for production mode to avoid breaking page rendering.
 */
public class LenientErrorHandler implements ErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LenientErrorHandler.class);

	@Override
	public void handleUnknownTag(String tagName, int line, int column, String template) {
		String location = formatLocation(template, line, column);
		LOGGER.warn("Unknown tag '{}' at {}", tagName, location);
	}

	@Override
	public void handleUnknownComponent(String componentName, int line, int column, String template) {
		String location = formatLocation(template, line, column);
		LOGGER.warn("Unknown component '{}' at {}", componentName, location);
	}

	@Override
	public void handleUnknownFilter(String filterName, int line, int column, String template) {
		String location = formatLocation(template, line, column);
		LOGGER.warn("Unknown filter '{}' at {}", filterName, location);
	}

	@Override
	public void handleRenderError(String message, int line, int column, String template, Throwable cause) {
		String location = formatLocation(template, line, column);
		if (cause != null) {
			LOGGER.warn("Render error at {}: {} ({})", location, message, cause.getMessage());
		} else {
			LOGGER.warn("Render error at {}: {}", location, message);
		}
	}

	private String formatLocation(String template, int line, int column) {
		if (template != null) {
			return String.format("%s:L%dC%d", template, line, column);
		}
		return String.format("L%dC%d", line, column);
	}
}
