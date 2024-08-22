package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.cache.CacheManager;
import com.github.thmarx.cms.api.cache.ICache;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.cms.ReadOnlyFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class CachedContentParser implements com.github.thmarx.cms.api.content.ContentParser{

	private final ContentParser defaultContentParser;
	private final ICache<String, Content> contentCache;	

	@Inject
	public CachedContentParser(final CacheManager cacheManager, final DefaultContentParser contentParser) {
		this.defaultContentParser = contentParser;
		if (ServerContext.IS_DEV) {
			contentCache = cacheManager.get("contentCache", 
					new CacheManager.CacheConfig(10l, Duration.ofMinutes(1)));
		} else {
			contentCache = cacheManager.get("contentCache", 
					new CacheManager.CacheConfig(0l, Duration.ofMinutes(1)));
		}
	}

	@Override
	public void clearCache() {
		contentCache.invalidate();
	}

	@Override
	public Content parse(final ReadOnlyFile contentFile) throws IOException {
		final String filename = contentFile.toAbsolutePath().toString();
		var cached = contentCache.get(filename);
		if (cached != null) {
			return cached;
		}
		var object = defaultContentParser.parse(contentFile);
		contentCache.put(filename, object);
		return object;
	}

	

	@Override
	public Map<String, Object> parseMeta(final ReadOnlyFile contentFile) throws IOException {
		return defaultContentParser.parseMeta(contentFile);
	}
}
