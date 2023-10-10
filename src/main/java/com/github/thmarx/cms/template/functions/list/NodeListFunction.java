/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.functions.list;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.template.functions.AbstractCurrentNodeFunction;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
class NodeListFunction extends AbstractCurrentNodeFunction {

	public static int DEFAULT_PAGE = 1;
	public static int DEFAUTL_PAGE_SIZE = 5;

	private boolean excludeIndexMd = false;

	private Predicate<MetaData.Node> nodeNameFilter = (node) -> {
		var filename = node.name();
		if (excludeIndexMd && "index.md".equals(filename)) {
			return false;
		}
		return true;
	};

	public NodeListFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser) {
		super(fileSystem, currentNode, contentParser);
	}

	public NodeListFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser, boolean excludeIndexMd) {
		this(fileSystem, currentNode, contentParser);
		this.excludeIndexMd = excludeIndexMd;
	}

	public Page<Node> list(String start, int page, int size, final Comparator<MetaData.Node> comparator) {
		return getNodes(start, page, size, comparator);
	}

	private Page getNodes(final String start, final int page, final int pageSize, final Comparator<MetaData.Node> comparator) {
		if (start.startsWith("/")) {
			return getNodesFromBase(fileSystem.resolve("content/"), start.substring(1), page, pageSize, comparator);
		} else if (start.equals(".")) {
			return getNodesFromBase(currentNode.getParent(), "", page, pageSize, comparator);
		} else if (start.startsWith("./")) {
			return getNodesFromBase(currentNode.getParent(), start.substring(2), page, pageSize, comparator);
		}
		return Page.EMPTY;
	}

	public Page<Node> getNodesFromBase(final Path base, final String start, final int page, final int pageSize, final Comparator<MetaData.Node> comparator) {
		try {
			List<Node> nodes = new ArrayList<>();
			final List<MetaData.Node> navNodes = fileSystem.listContent(base, start);
			final Path contentBase = fileSystem.resolve("content/");
			long total = navNodes.stream().filter(nodeNameFilter).count();
			int skipCount = (page - 1) * pageSize;

			navNodes.stream().filter(nodeNameFilter)
					.sorted(comparator)
					.skip(skipCount)
					.limit(pageSize)
					.forEach(node -> {
						var path = contentBase.resolve(node.uri());
						var name = getName(node);
						var md = parse(path);
						final Node navNode = new Node(name, getUrl(path), md.get().content());
						nodes.add(navNode);
					});

			int totalPages = (int) Math.ceil((float) total / pageSize);
			return new Page<Node>(pageSize, totalPages, page, nodes);
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return Page.EMPTY;
	}

}
