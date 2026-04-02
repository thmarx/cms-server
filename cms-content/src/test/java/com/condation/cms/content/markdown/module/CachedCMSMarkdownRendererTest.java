package com.condation.cms.content.markdown.module;

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

import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.core.cache.LocalCacheProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

/**
 * Tests for cached CMS markdown renderer.
 */
public class CachedCMSMarkdownRendererTest {

	private CachedCMSMarkdownRenderer cachedRenderer;
	private CacheManager cacheManager;
	private ICache<String, String> cache;

	@BeforeEach
	public void setup() {
		cacheManager = new CacheManager(new LocalCacheProvider());

		cache = cacheManager.get("markdown-module-test",
			new CacheManager.CacheConfig(100L, Duration.ofMinutes(10)));
		cachedRenderer = new CachedCMSMarkdownRenderer(cache);
	}

	@Test
	public void testCacheHit() {
		String md = "# Hello World\n\nThis is a **test**.";

		// First call - cache miss, should render and cache
		String html1 = cachedRenderer.render(md);
		Assertions.assertThat(html1).contains("<h1");
		Assertions.assertThat(html1).contains("Hello World");
		Assertions.assertThat(cache.contains(md)).isTrue();

		// Second call - cache hit, should return same result
		String html2 = cachedRenderer.render(md);
		Assertions.assertThat(html2).isEqualTo(html1);
		Assertions.assertThat(cache.contains(md)).isTrue();
	}

	@Test
	public void testExcerpt() {
		String md = "# Title\n\nSome text here.";
		String excerpt = cachedRenderer.excerpt(md, 5);
		
		Assertions.assertThat(excerpt).isEqualTo("Title");
	}

}
