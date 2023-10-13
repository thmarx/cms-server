/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ContentParser {

	private final FileSystem fileSystem;
	private final Cache<String, Content> contentCache;

	private final Yaml yaml = new Yaml();
	
	public ContentParser(final FileSystem fileSystem) {
		this.fileSystem = fileSystem;

		var builder = Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(1));
		if (Server.DEV_MODE) {
			builder.maximumSize(0);
		}
		contentCache = builder.build();
	}

	public void clearCache() {
		contentCache.invalidateAll();
	}

	public Content parse(final Path contentFile) throws IOException {
		final String filename = contentFile.toAbsolutePath().toString();
		var cached = contentCache.getIfPresent(filename);
		if (cached != null) {
			return cached;
		}
		var object = _parse(contentFile);
		contentCache.put(filename, object);
		return object;
	}

	private Content _parse(final Path contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return new Content(readContent.content(), _parseMeta(readContent));
	}
    
    private Map<String, Object> _parseMeta (ContentRecord content) {
        if (Strings.isNullOrEmpty(content.meta.trim())) {
            return Collections.emptyMap();
        }
        return yaml.load(content.meta());
    }

	public Map<String, Object> parseMeta(final Path contentFile) throws IOException {
		ContentRecord readContent = readContent(contentFile);

		return _parseMeta(readContent);
	}

	private ContentRecord readContent(final Path contentFile) throws IOException {
		var fileContent = fileSystem.loadLines(contentFile);

		StringBuilder contentBuilder = new StringBuilder();
		StringBuilder metaBuilder = new StringBuilder();

		AtomicBoolean inFrontMatter = new AtomicBoolean(false);
		AtomicInteger counter = new AtomicInteger(0);
		fileContent.stream().map(String::trim).forEach((line) -> {
			if (line.trim().equals("---")) {
				counter.incrementAndGet();
			}
			if (inFrontMatter.get() == false && line.trim().equals("---")) {
				inFrontMatter.set(true);
                return;
			} else if (inFrontMatter.get() && line.trim().equals("---")) {
				inFrontMatter.set(false);
                return;
			}
			if (inFrontMatter.get()) {
				metaBuilder.append(line).append("\r\n");
			} else {
				contentBuilder.append(line).append("\r\n");
			}
		});
		if (counter.get() != 2) {
			log.error("error reading content file, wrong format");
			throw new RuntimeException("error reading content file, wrong format");
		}
		
		return new ContentRecord(contentBuilder.toString(), metaBuilder.toString());
	}

	private record ContentRecord(String content, String meta) {}

	public record Content(String content, Map<String, Object> meta) {}
}
