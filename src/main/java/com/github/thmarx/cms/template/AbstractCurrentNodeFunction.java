/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.template.navigation.NavigationFunction;
import freemarker.template.TemplateMethodModelEx;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public abstract class AbstractCurrentNodeFunction implements TemplateMethodModelEx {

	protected final Path contentBase;
	protected final Path currentNode;
	protected final ContentParser contentParser;

	protected String getUrl(Path node) {
		StringBuilder sb = new StringBuilder();

		while (node != null && !node.equals(contentBase)) {

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
			Logger.getLogger(NavigationFunction.class.getName()).log(Level.SEVERE, null, ex);
		}
		return Optional.empty();
	}

	protected Optional<String> getName(Path node) {
		var md = parse(node);
		if (md.isEmpty()) {
			return Optional.empty();
		}
		if (md.get().meta().containsKey("menu.title")) {
			return Optional.of((String) md.get().meta().get("menu.title"));
		}
		if (md.get().meta().containsKey("title")) {
			return Optional.of((String) md.get().meta().get("title"));
		}

		return Optional.empty();
	}
}
