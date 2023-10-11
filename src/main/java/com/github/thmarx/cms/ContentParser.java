/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.utils.BooleanUtil;
import com.github.thmarx.cms.utils.DateUtil;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class ContentParser {

	private final FileSystem fileSystem;
	private final Cache<String, Content> contentCache;

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

//	public Content parse (final String file) throws IOException {
//		return parse(contentBase.resolve(file));
//	}
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
		var fileContent = fileSystem.loadLines(contentFile);

		StringBuilder content = new StringBuilder();
		Map<String, Object> meta = new HashMap<>();

		AtomicBoolean inMeta = new AtomicBoolean(true);
		fileContent.forEach((line) -> {
			if (line.startsWith("-----")) {
				inMeta.set(false);
				return;
			}
			if (inMeta.get()) {
				if (Strings.isNullOrEmpty(line)) {
					return;
				}
				var entry = createEntry(line);
				meta.put(entry.getKey(), entry.getValue());
			} else {
				content.append(line).append(System.lineSeparator());
			}
		});

		return new Content(content.toString(), meta);
	}

	public Map<String, Object> parseMeta(final Path contentFile) throws IOException {
		var fileContent = fileSystem.loadLines(contentFile);

		Map<String, Object> meta = new HashMap<>();

		AtomicBoolean inMeta = new AtomicBoolean(true);
		fileContent.forEach((line) -> {
			if (line.startsWith("-----")) {
				inMeta.set(false);
				return;
			}
			if (inMeta.get()) {
				if (Strings.isNullOrEmpty(line)) {
					return;
				}
				var entry = createEntry(line);
				meta.put(entry.getKey(), entry.getValue());
			}
		});

		return meta;
	}

	private Map.Entry<String, Object> createEntry(final String line) {
		var parts = line.split(":");
		final String key = parts[0];
		final String value = parts[1];
		if (isArray(value)) {
			var arrayValues = value.substring(1, value.length() - 1);
			var list = Stream.of(arrayValues.split(","))
					.map(String::trim)
					.map((val) -> val.replace("[", ""))
					.map((val) -> val.replace("]", ""))
					.collect(Collectors.toList());
			return new AbstractMap.SimpleEntry<>(key, list);
		} else if (DateUtil.isDate(value)) {
			return new AbstractMap.SimpleEntry<>(key, DateUtil.toDate(value));
		} else if (BooleanUtil.isBoolean(value)) {
			return new AbstractMap.SimpleEntry<>(key, BooleanUtil.toBoolean(value));
		} else {
			return new AbstractMap.SimpleEntry<>(key, value.trim());
		}
	}

	private boolean isArray(final String value) {
		var trimmed = value.trim();
		return trimmed.startsWith("[") && trimmed.endsWith("]");

	}

	public record Content(String content, Map<String, Object> meta) {

	}
}
