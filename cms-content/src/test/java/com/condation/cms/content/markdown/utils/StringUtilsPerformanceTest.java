package com.condation.cms.content.markdown.utils;

/*-
 * #%L
 * cms-content
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Performance tests for optimized StringUtils.
 */
public class StringUtilsPerformanceTest {

	@Test
	public void testEscapePerformance() {
		// Generate large markdown with many escape sequences
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			md.append("This is \\* a \\# test \\[ with \\] many \\{ escape \\} sequences\n");
		}
		String largeMarkdown = md.toString();

		// Warm up
		for (int i = 0; i < 10; i++) {
			StringUtils.escape(largeMarkdown);
		}

		// Measure performance
		long start = System.nanoTime();
		String escaped = StringUtils.escape(largeMarkdown);
		long duration = System.nanoTime() - start;

		System.out.printf("Escaped %d chars in %d ms%n",
				largeMarkdown.length(),
				duration / 1_000_000);

		// Verify correctness
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#42;"); // escaped *
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#35;"); // escaped #

		// Should complete in reasonable time (< 100ms for 70k chars)
		Assertions.assertThat(duration).isLessThan(100_000_000L);
	}

	@Test
	public void testUnescapePerformance() {
		String html = "Test ".repeat(10000) + "AMP#PLACE#HOLDER" + " more text ".repeat(1000);

		long start = System.nanoTime();
		String unescaped = StringUtils.unescape(html);
		long duration = System.nanoTime() - start;

		System.out.printf("Unescaped %d chars in %d ms%n",
				html.length(),
				duration / 1_000_000);

		Assertions.assertThat(unescaped).contains("&");
		Assertions.assertThat(duration).isLessThan(50_000_000L);
	}

	@Test
	public void testEscapeCorrectness() {
		String md = "\\# \\* \\` \\_ \\{ \\} \\[ \\] \\< \\> \\( \\) \\+ \\- \\. \\! \\|";
		String escaped = StringUtils.escape(md);

		// All special chars should be escaped
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#35;");  // #
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#42;");  // *
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#96;");  // `
		Assertions.assertThat(escaped).contains("AMP#PLACE#HOLDER#95;");  // _

		// Unescape should produce valid HTML entities
		String unescaped = StringUtils.unescape(escaped);
		Assertions.assertThat(unescaped).contains("&#35;");
		Assertions.assertThat(unescaped).contains("&#42;");
	}

	@Test
	public void testEscapeEmptyAndNull() {
		Assertions.assertThat(StringUtils.escape(null)).isNull();
		Assertions.assertThat(StringUtils.escape("")).isEmpty();
		Assertions.assertThat(StringUtils.unescape(null)).isNull();
		Assertions.assertThat(StringUtils.unescape("")).isEmpty();
	}

	@Test
	public void testEscapeNoSpecialChars() {
		String md = "This is normal text without any special markdown characters.";
		String escaped = StringUtils.escape(md);

		// Should return quickly and unchanged
		Assertions.assertThat(escaped).isEqualTo(md);
	}
}
