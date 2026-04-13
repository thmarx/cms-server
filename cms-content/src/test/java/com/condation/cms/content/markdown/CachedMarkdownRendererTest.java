package com.condation.cms.content.markdown;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.core.cache.LocalCacheProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

/**
 * Tests for cached markdown renderer using CMS ICache infrastructure.
 */
public class CachedMarkdownRendererTest {

	private CMSMarkdown markdown;
	private CachedMarkdownRenderer cachedRenderer;
	private CacheManager cacheManager;

	@BeforeEach
	public void setup() {
		Options options = Options.all();
		markdown = new CMSMarkdown(options);
		cacheManager = new CacheManager(new LocalCacheProvider());

		ICache<String, String> cache = cacheManager.get("markdown-test",
			new CacheManager.CacheConfig(100L, Duration.ofMinutes(10)));
		cachedRenderer = new CachedMarkdownRenderer(markdown, cache);
	}

	@Test
	public void testCacheHit() throws IOException {
		String md = "# Hello World\n\nThis is a **test**.";

		// First call - cache miss, should render and cache
		String html1 = cachedRenderer.render(md);
		Assertions.assertThat(html1).contains("<h1");
		Assertions.assertThat(html1).contains("Hello World");
		Assertions.assertThat(cachedRenderer.isCached(md)).isTrue();

		// Second call - cache hit, should return same result
		String html2 = cachedRenderer.render(md);
		Assertions.assertThat(html2).isEqualTo(html1);
		Assertions.assertThat(cachedRenderer.isCached(md)).isTrue();
	}

	@Test
	public void testCacheMiss() throws IOException {
		String md1 = "# First";
		String md2 = "# Second";

		cachedRenderer.render(md1);
		cachedRenderer.render(md2);

		// Both should be cached
		Assertions.assertThat(cachedRenderer.isCached(md1)).isTrue();
		Assertions.assertThat(cachedRenderer.isCached(md2)).isTrue();
	}

	@Test
	public void testClearCache() throws IOException {
		String md = "# Test";
		cachedRenderer.render(md);

		Assertions.assertThat(cachedRenderer.isCached(md)).isTrue();

		cachedRenderer.clearCache();

		Assertions.assertThat(cachedRenderer.isCached(md)).isFalse();
	}

	@Test
	public void testPerformanceImprovement() throws IOException {
		// Large markdown document
		StringBuilder md = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			md.append("# Heading ").append(i).append("\n\n");
			md.append("This is **paragraph** ").append(i).append(" with *italic* text.\n\n");
			md.append("- List item 1\n");
			md.append("- List item 2\n\n");
		}
		String largeMarkdown = md.toString();

		// First render (uncached)
		long start1 = System.nanoTime();
		String html1 = cachedRenderer.render(largeMarkdown);
		long duration1 = System.nanoTime() - start1;

		// Second render (cached)
		long start2 = System.nanoTime();
		String html2 = cachedRenderer.render(largeMarkdown);
		long duration2 = System.nanoTime() - start2;

		System.out.printf("Uncached: %d ms, Cached: %d ms, Speedup: %.1fx%n",
				duration1 / 1_000_000,
				duration2 / 1_000_000,
				(double) duration1 / duration2);

		// Results should be identical
		Assertions.assertThat(html2).isEqualTo(html1);

		// Cached should be MUCH faster (>10x)
		Assertions.assertThat(duration2).isLessThan(duration1 / 10);
	}

	@Test
	public void testDisabledCache() throws IOException {
		// Create renderer without cache (pass-through mode)
		CachedMarkdownRenderer noCacheRenderer = new CachedMarkdownRenderer(markdown);

		String md = "# Test";
		noCacheRenderer.render(md);
		noCacheRenderer.render(md);

		// No caching should occur
		Assertions.assertThat(noCacheRenderer.isCacheEnabled()).isFalse();
		Assertions.assertThat(noCacheRenderer.isCached(md)).isFalse();
	}

	@Test
	public void testComplexMarkdown() throws IOException {
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

				[Link](http://example.com)

				> Blockquote text
				""";

		String html1 = cachedRenderer.render(complexMd);
		String html2 = cachedRenderer.render(complexMd);

		// Both renders should produce identical output
		Assertions.assertThat(html1).isEqualTo(html2);
		// Second render should be cached
		Assertions.assertThat(cachedRenderer.isCached(complexMd)).isTrue();
	}

	@Test
	public void testInvalidateSpecificEntry() throws IOException {
		String md1 = "# First";
		String md2 = "# Second";

		cachedRenderer.render(md1);
		cachedRenderer.render(md2);

		Assertions.assertThat(cachedRenderer.isCached(md1)).isTrue();
		Assertions.assertThat(cachedRenderer.isCached(md2)).isTrue();

		// Invalidate only md1
		cachedRenderer.invalidate(md1);

		Assertions.assertThat(cachedRenderer.isCached(md1)).isFalse();
		Assertions.assertThat(cachedRenderer.isCached(md2)).isTrue();
	}
}
