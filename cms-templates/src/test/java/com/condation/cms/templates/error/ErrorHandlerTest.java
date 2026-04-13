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

import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.exceptions.UnknownTagException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for error handling strategies.
 */
public class ErrorHandlerTest {

	@Test
	public void testStrictErrorHandler() {
		StrictErrorHandler handler = new StrictErrorHandler();

		// Unknown tag should throw
		Assertions.assertThatThrownBy(() ->
						handler.handleUnknownTag("foo", 10, 5, "test.html"))
				.isInstanceOf(UnknownTagException.class)
				.hasMessageContaining("Unknown tag: foo");

		// Unknown component should throw
		Assertions.assertThatThrownBy(() ->
						handler.handleUnknownComponent("bar", 10, 5, "test.html"))
				.isInstanceOf(RenderException.class)
				.hasMessageContaining("Unknown component: bar");

		// Unknown filter should throw
		Assertions.assertThatThrownBy(() ->
						handler.handleUnknownFilter("baz", 10, 5, "test.html"))
				.isInstanceOf(RenderException.class)
				.hasMessageContaining("Unknown filter: baz");
	}

	@Test
	public void testLenientErrorHandler() {
		LenientErrorHandler handler = new LenientErrorHandler();

		// Should not throw, just log
		Assertions.assertThatCode(() -> {
			handler.handleUnknownTag("foo", 10, 5, "test.html");
			handler.handleUnknownComponent("bar", 10, 5, "test.html");
			handler.handleUnknownFilter("baz", 10, 5, "test.html");
			handler.handleRenderError("Error message", 10, 5, "test.html", null);
		}).doesNotThrowAnyException();
	}

	@Test
	public void testCustomErrorHandler() {
		// Custom handler that collects errors instead of throwing
		List<String> errors = new ArrayList<>();

		ErrorHandler customHandler = new ErrorHandler() {
			@Override
			public void handleUnknownTag(String tagName, int line, int column, String template) {
				errors.add("Unknown tag: " + tagName + " at " + line + ":" + column);
			}

			@Override
			public void handleUnknownComponent(String componentName, int line, int column, String template) {
				errors.add("Unknown component: " + componentName + " at " + line + ":" + column);
			}

			@Override
			public void handleUnknownFilter(String filterName, int line, int column, String template) {
				errors.add("Unknown filter: " + filterName + " at " + line + ":" + column);
			}

			@Override
			public void handleRenderError(String message, int line, int column, String template, Throwable cause) {
				errors.add("Render error: " + message + " at " + line + ":" + column);
			}
		};

		customHandler.handleUnknownTag("foo", 10, 5, null);
		customHandler.handleUnknownComponent("bar", 20, 10, null);

		Assertions.assertThat(errors).hasSize(2);
		Assertions.assertThat(errors.get(0)).contains("Unknown tag: foo");
		Assertions.assertThat(errors.get(1)).contains("Unknown component: bar");
	}

	@Test
	public void testStrictErrorHandlerWithCause() {
		StrictErrorHandler handler = new StrictErrorHandler();

		Exception cause = new RuntimeException("Original error");

		Assertions.assertThatThrownBy(() ->
						handler.handleRenderError("Wrapped error", 10, 5, "test.html", cause))
				.isInstanceOf(RenderException.class)
				.hasMessageContaining("Wrapped error")
				.hasMessageContaining("Original error");
	}
}
