/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.functions;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.template.functions.navigation.NavigationFunction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCurrentNodeFunction {

	protected final FileSystem fileSystem;
	protected final Path currentNode;
	protected final ContentParser contentParser;

	protected String getUrl(Path node) {
		StringBuilder sb = new StringBuilder();

		while (node != null && !node.equals(fileSystem.resolve("content/"))) {

			var filename = node.getFileName().toString();
			if (!filename.equals("index.md")) {
				if (filename.endsWith(".md")) {
					filename = filename.substring(0, filename.length() - 3);
				}
				sb.insert(0, filename);
				sb.insert(0, "/");
			}
			node = node.getParent();
		}

		var url = sb.toString();

		return "".equals(url) ? "/" : url;
	}

	protected Optional<ContentParser.Content> parse(Path node) {
		try {
			//Path rel = contentBase.relativize(node);
			if (Files.isDirectory(node)) {
				node = node.resolve("index.md");
			}
			var md = contentParser.parse(node);

			return Optional.of(md);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return Optional.empty();
	}

	protected String getName(MetaData.Node node) {
		if (node.data().containsKey("menu.title")) {
			return (String) node.data().get("menu.title");
		}
		if (node.data().containsKey("title")) {
			return (String) node.data().get("title");
		}

		return node.name();
	}
}
