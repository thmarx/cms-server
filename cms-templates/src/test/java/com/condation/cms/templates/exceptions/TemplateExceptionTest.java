package com.condation.cms.templates.exceptions;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for enhanced template error reporting.
 */
public class TemplateExceptionTest {

	@Test
	public void testBasicErrorMessage() {
		ParserException ex = new ParserException("Undefined tag: foo", 10, 5);

		String message = ex.getMessage();
		Assertions.assertThat(message).contains("Undefined tag: foo");
		Assertions.assertThat(message).contains("line 10");
		Assertions.assertThat(message).contains("column 5");
	}

	@Test
	public void testErrorWithTemplateName() {
		ParserException ex = new ParserException("Undefined tag: foo", 10, 5, "layout.html");

		String message = ex.getMessage();
		Assertions.assertThat(message).contains("layout.html");
		Assertions.assertThat(message).contains("line 10");
	}

	@Test
	public void testErrorWithSourceSnippet() {
		String snippet = TemplateException.extractSourceSnippet(
				"line 1\nline 2\nline 3 {% foo %}\nline 4\nline 5",
				3, // error on line 3
				1  // show 1 line before and after
		);

		Assertions.assertThat(snippet).contains("line 2");
		Assertions.assertThat(snippet).contains("line 3");
		Assertions.assertThat(snippet).contains("line 4");
		Assertions.assertThat(snippet).contains("→"); // error marker
		Assertions.assertThat(snippet).doesNotContain("line 1"); // out of context
		Assertions.assertThat(snippet).doesNotContain("line 5"); // out of context

		System.out.println("\nError with snippet:\n" + snippet);
	}

	@Test
	public void testFullErrorWithSnippet() {
		String template = """
				<html>
				<body>
				  <div>
				    {% unknownTag %}
				  </div>
				</body>
				</html>
				""";

		String snippet = TemplateException.extractSourceSnippet(template, 4, 2);
		ParserException ex = new ParserException(
				"Unknown tag 'unknownTag'",
				4, 7,
				"template.html",
				snippet
		);

		String message = ex.getMessage();
		Assertions.assertThat(message).contains("Unknown tag 'unknownTag'");
		Assertions.assertThat(message).contains("template.html");
		Assertions.assertThat(message).contains("line 4");
		Assertions.assertThat(message).contains("unknownTag");

		System.out.println("\nFull error message:\n" + message);
	}

	@Test
	public void testSnippetAtStart() {
		String template = "line 1\nline 2\nline 3";
		String snippet = TemplateException.extractSourceSnippet(template, 1, 2);

		Assertions.assertThat(snippet).contains("line 1");
		Assertions.assertThat(snippet).contains("→"); // error marker on line 1
	}

	@Test
	public void testSnippetAtEnd() {
		String template = "line 1\nline 2\nline 3";
		String snippet = TemplateException.extractSourceSnippet(template, 3, 2);

		Assertions.assertThat(snippet).contains("line 3");
		Assertions.assertThat(snippet).contains("→"); // error marker on line 3
	}

	@Test
	public void testSnippetWithInvalidLine() {
		String template = "line 1\nline 2";
		String snippet = TemplateException.extractSourceSnippet(template, 10, 1);

		Assertions.assertThat(snippet).isNull();
	}

	@Test
	public void testSnippetWithEmptyTemplate() {
		String snippet = TemplateException.extractSourceSnippet("", 1, 1);
		Assertions.assertThat(snippet).isNull();

		String snippet2 = TemplateException.extractSourceSnippet(null, 1, 1);
		Assertions.assertThat(snippet2).isNull();
	}

	@Test
	public void testColumnMarker() {
		String marker = TemplateException.createColumnMarker(5);
		Assertions.assertThat(marker).isEqualTo("    ^");

		String marker2 = TemplateException.createColumnMarker(1);
		Assertions.assertThat(marker2).isEqualTo("^");
	}

	@Test
	public void testRenderException() {
		RenderException ex = new RenderException("Variable not found", 5, 10, "index.html");

		String message = ex.getMessage();
		Assertions.assertThat(message).contains("Variable not found");
		Assertions.assertThat(message).contains("index.html");
		Assertions.assertThat(message).contains("line 5");
	}

	@Test
	public void testAccessors() {
		ParserException ex = new ParserException("Test", 10, 5, "test.html");

		Assertions.assertThat(ex.getLine()).isEqualTo(10);
		Assertions.assertThat(ex.getColumn()).isEqualTo(5);
		Assertions.assertThat(ex.getTemplateName()).isEqualTo("test.html");
	}
}
