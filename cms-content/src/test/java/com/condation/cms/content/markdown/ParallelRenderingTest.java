package com.condation.cms.content.markdown;

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

import java.io.IOException;

/**
 * Tests for parallel block rendering optimization.
 */
public class ParallelRenderingTest {

	@Test
	public void testParallelVsSequential() throws IOException {
		// Create large document with many blocks
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 50; i++) {
			md.append("# Heading ").append(i).append("\n\n");
			md.append("This is **paragraph** ").append(i).append(" with *italic* text.\n\n");
			md.append("- List item 1\n");
			md.append("- List item 2\n\n");
		}
		String largeMarkdown = md.toString();

		Options options = Options.all();

		// Sequential rendering
		CMSMarkdown sequentialRenderer = new CMSMarkdown(options, false, 10);
		long start1 = System.nanoTime();
		String html1 = sequentialRenderer.render(largeMarkdown);
		long duration1 = System.nanoTime() - start1;

		// Parallel rendering
		CMSMarkdown parallelRenderer = new CMSMarkdown(options, true, 10);
		long start2 = System.nanoTime();
		String html2 = parallelRenderer.render(largeMarkdown);
		long duration2 = System.nanoTime() - start2;

		System.out.printf("Sequential: %d ms, Parallel: %d ms, Speedup: %.1fx%n",
				duration1 / 1_000_000,
				duration2 / 1_000_000,
				(double) duration1 / duration2);

		// Results should be identical
		Assertions.assertThat(html2).isEqualTo(html1);

		// Note: Parallel rendering may have overhead for small workloads
		// Just verify it produces correct output, not necessarily faster
		// (Performance varies based on CPU cores, JVM warmup, etc.)
	}

	@Test
	public void testSmallDocumentUsesSequential() throws IOException {
		// Small document (< 10 blocks)
		String smallMd = "# Title\n\nParagraph with **bold**.";

		Options options = Options.all();
		CMSMarkdown renderer = new CMSMarkdown(options, true, 10);

		String html = renderer.render(smallMd);

		// Should still work correctly
		Assertions.assertThat(html).contains("<h1");
		Assertions.assertThat(html).contains("<strong>bold</strong>");
	}

	@Test
	public void testParallelDisabled() throws IOException {
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			md.append("# Heading ").append(i).append("\n\n");
		}

		Options options = Options.all();
		CMSMarkdown renderer = new CMSMarkdown(options, false, 5);

		String html = renderer.render(md.toString());

		// Should work even with parallel disabled
		Assertions.assertThat(html).contains("<h1");
	}

	@Test
	public void testBlockOrderPreserved() throws IOException {
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			md.append("## Section ").append(i).append("\n\n");
			md.append("Content ").append(i).append("\n\n");
		}

		Options options = Options.all();
		CMSMarkdown parallelRenderer = new CMSMarkdown(options, true, 10);
		CMSMarkdown sequentialRenderer = new CMSMarkdown(options, false, 10);

		String parallelHtml = parallelRenderer.render(md.toString());
		String sequentialHtml = sequentialRenderer.render(md.toString());

		// Order must be preserved
		Assertions.assertThat(parallelHtml).isEqualTo(sequentialHtml);
	}

	@Test
	public void testLargeDocumentBenchmark() throws IOException {
		// Very large document (100+ blocks) to see parallel benefit
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			md.append("# Heading ").append(i).append("\n\n");
			md.append("This is **paragraph** ").append(i).append(" with *italic* and `code`.\n\n");
			md.append("- List item 1\n");
			md.append("- List item 2 with **formatting**\n");
			md.append("- List item 3 with *emphasis*\n\n");
			md.append("> Quote block ").append(i).append("\n\n");
		}
		String largeMarkdown = md.toString();

		Options options = Options.all();

		// Warm up JVM
		CMSMarkdown warmup = new CMSMarkdown(options, true, 10);
		warmup.render(largeMarkdown);

		// Sequential rendering
		CMSMarkdown sequentialRenderer = new CMSMarkdown(options, false, 10);
		long start1 = System.nanoTime();
		String html1 = sequentialRenderer.render(largeMarkdown);
		long duration1 = System.nanoTime() - start1;

		// Parallel rendering
		CMSMarkdown parallelRenderer = new CMSMarkdown(options, true, 10);
		long start2 = System.nanoTime();
		String html2 = parallelRenderer.render(largeMarkdown);
		long duration2 = System.nanoTime() - start2;

		double speedup = (double) duration1 / duration2;
		System.out.printf("LARGE DOC - Sequential: %d ms, Parallel: %d ms, Speedup: %.2fx%n",
				duration1 / 1_000_000,
				duration2 / 1_000_000,
				speedup);

		// Results should be identical
		Assertions.assertThat(html2).isEqualTo(html1);

		// For very large documents, parallel should show benefit
		System.out.println("Speedup achieved: " + speedup + "x");
	}

	@Test
	public void testComplexMarkdownParallel() throws IOException {
		String complexMd = """
				# Title

				This is a paragraph with **bold**, *italic*, and `code`.

				## Subtitle

				- List item 1
				- List item 2
				  - Nested item

				```java
				public class Test {
				    void method() {}
				}
				```

				### Another heading

				[Link](http://example.com)

				> Blockquote text

				| Header 1 | Header 2 |
				|----------|----------|
				| Cell 1   | Cell 2   |

				---

				Final paragraph.
				""";

		Options options = Options.all();
		CMSMarkdown renderer = new CMSMarkdown(options, true, 10);

		String html = renderer.render(complexMd);

		// Should handle complex markdown correctly
		Assertions.assertThat(html).contains("<h1");
		Assertions.assertThat(html).contains("<code");
		Assertions.assertThat(html).contains("<blockquote");
		Assertions.assertThat(html).contains("<table");
	}
}
