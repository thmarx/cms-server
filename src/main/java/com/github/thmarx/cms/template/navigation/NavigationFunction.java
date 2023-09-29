/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.navigation;

import com.github.thmarx.cms.ContentParser;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class NavigationFunction implements TemplateMethodModelEx {

	public final Path contentBase;
	public final Path currentNode;
	public final ContentParser parser;
	
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		if (arguments.size() == 0) {
			return Collections.emptyList();
		}
		String start = ((SimpleScalar) arguments.get(0)).getAsString();
		int depth = 0;
		if (arguments.size() == 2) {
			depth = ((SimpleNumber) arguments.get(1)).getAsNumber().intValue();
		}
		
		return getNodes(start, depth);
	}

	private List<NavNode> getNodes(final String start, final int depth) {
		if (start.startsWith("/")) {
			return getNodesFromBase(contentBase, start.substring(1), depth);
		} else if (start.equals(".")) {
			return getNodesFromBase(currentNode.getParent(), "", depth);
		} else if (start.startsWith("./")) {
			return getNodesFromBase(currentNode.getParent(), start.substring(2), depth);
		}
		return Collections.emptyList();
	}

	public List<NavNode> getNodesFromBase(final Path base, final String start, final int depth) {
		try {
			List<NavNode> nodes = new ArrayList<>();
			var startPath = base.resolve(start);
			Files.list(startPath).forEach(path -> {
				var filename = path.getFileName().toString();
				if (filename.startsWith("_")) {
					return;
				}
				if (filename.endsWith(".md")) {
					filename = filename.substring(0, filename.length() - 3);
				}
				var name = getName(path);
				final NavNode node = new NavNode(name.isPresent() ? name.get() : filename, getUrl(path));
				if (isCurrentNode(path)) {
					node.setCurrent(true);
				}
				nodes.add(node);
			});
			return nodes;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return Collections.emptyList();
	}

	private boolean isCurrentNode(final Path node) {
		Path nodeIndex;
		if ("index.md".equals(node.getFileName().toString())) {
			nodeIndex = node;
		} else {
			nodeIndex = node.resolve("index.md");
		}
		return node.equals(currentNode) || currentNode.equals(nodeIndex);
	}

	private String getUrl(Path node) {
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
	
	private Optional<String> getName (Path node) {
		try {
			//Path rel = contentBase.relativize(node);
			if (Files.isDirectory(node)) {
				node = node.resolve("index.md");
			}
			var md = parser.parse(node);
			if (md.meta().containsKey("menu.title")) {
				return Optional.of((String)md.meta().get("menu.title"));
			}
			if (md.meta().containsKey("title")) {
				return Optional.of((String)md.meta().get("title"));
			}
		} catch (IOException ex) {
			Logger.getLogger(NavigationFunction.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return Optional.empty();
	}
}
