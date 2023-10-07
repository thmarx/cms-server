/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.functions.navigation;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.template.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.utils.PathUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class NavigationFunction extends AbstractCurrentNodeFunction {

	private static final int DEFAULT_DEPTH = 0;

	public NavigationFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser) {
		super(fileSystem, currentNode, contentParser);
	}

	public List<NavNode> list(final String start) {
		return getNodes(start, DEFAULT_DEPTH);
	}

	public List<NavNode> list(final String start, final int depth) {
		return getNodes(start, depth);
	}

	private List<NavNode> getNodes(final String start, final int depth) {
		if (start.startsWith("/")) {
			return getNodesFromBase(fileSystem.resolve("content/"), start.substring(1), depth);
		} else if (start.equals(".")) {
			return getNodesFromBase(currentNode.getParent(), "", depth);
		} else if (start.startsWith("./")) {
			return getNodesFromBase(currentNode.getParent(), start.substring(2), depth);
		}
		return Collections.emptyList();
	}

	public List<NavNode> getNodesFromBase(final Path base, final String start, final int depth) {
		try {
			final Path contentBase = fileSystem.resolve("content/");
			List<NavNode> nodes = new ArrayList<>();
			var startPath = base.resolve(start);
			Files.list(startPath)
					.filter(path -> {
						var uri = PathUtil.toUri(path, contentBase);

						final Optional<MetaData.Node> byUri = fileSystem.getMetaData().byUri(uri);
						if (byUri.isPresent()) {
							MetaData.Node node = byUri.get();
							return !(boolean) node.data().getOrDefault("draft", false);
						}
						
						return true;
					})
					.forEach(path -> {
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
}
