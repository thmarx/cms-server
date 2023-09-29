/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	final Path contentBase;
	
	public ContentParser (final Path contentBase) {
		this.contentBase = contentBase;
	}
	
	public Content parse (final String file) throws IOException {
		return parse(contentBase.resolve(file));
	}
	
	public Content parse (final Path contentFile) throws IOException {
		var fileContent = Files.readAllLines(contentFile, StandardCharsets.UTF_8);
		
		StringBuilder content = new StringBuilder();
		Map<String, Object> meta = new HashMap<>();
		
		AtomicBoolean inMeta = new AtomicBoolean(true);
		fileContent.forEach((line) -> {
			if (line.startsWith("-----")) {
				inMeta.set(false);
				return;
			}
			if (inMeta.get()){
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

	private Map.Entry<String, Object> createEntry (final String line) {
		var parts = line.split(":");
		final String key = parts[0]; 
		final String value = parts[1];
		if (isArray(value)) {
			var arrayValues = value.substring(1, value.length()-1);
			var list = Stream.of(arrayValues.split(","))
					.map(String::trim)
					.map((val) -> val.replace("[", ""))
					.map((val) -> val.replace("]", ""))
					.collect(Collectors.toList());
			return new AbstractMap.SimpleEntry<>(key, list);
		} else {
			return new AbstractMap.SimpleEntry<>(key, value.trim());
		}
	}
	
	private boolean isArray (final String value) {
		var trimmed = value.trim();
		return trimmed.startsWith("[") && trimmed.endsWith("]");
			
	} 
	
	public record Content(String content, Map<String,Object> meta) {}
}
