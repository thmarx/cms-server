/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.list;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.template.AbstractCurrentNodeFunction;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author t.marx
 */
public class NodeListFunction extends AbstractCurrentNodeFunction {

	public static int DEFAULT_PAGE = 1;
	public static int DEFAUTL_PAGE_SIZE = 5;

	private boolean excludeIndexMd = false;

	private Predicate<Path> fileNameFilter = (path) -> {
		var filename = path.getFileName().toString();
		if (excludeIndexMd && "index.md".equals(filename)) {
			return false;
		}
		return !filename.startsWith("_");
	};

	public NodeListFunction(Path contentBase, Path currentNode, ContentParser contentParser) {
		super(contentBase, currentNode, contentParser);
	}

	public NodeListFunction(Path contentBase, Path currentNode, ContentParser contentParser, boolean excludeIndexMd) {
		this(contentBase, currentNode, contentParser);
		this.excludeIndexMd = excludeIndexMd;
	}

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		String start = ((SimpleScalar) arguments.get(0)).getAsString();

		int page = DEFAULT_PAGE;
		int size = DEFAUTL_PAGE_SIZE;
		if (arguments.size() > 1) {
			page = ((SimpleNumber) arguments.get(1)).getAsNumber().intValue();
		}
		if (arguments.size() > 2) {
			size = ((SimpleNumber) arguments.get(2)).getAsNumber().intValue();
		}

		return getNodes(start, page, size);
	}

	private Page getNodes(final String start, final int page, final int pageSize) {
		if (start.startsWith("/")) {
			return getNodesFromBase(contentBase, start.substring(1), page, pageSize);
		} else if (start.equals(".")) {
			return getNodesFromBase(currentNode.getParent(), "", page, pageSize);
		} else if (start.startsWith("./")) {
			return getNodesFromBase(currentNode.getParent(), start.substring(2), page, pageSize);
		}
		return Page.EMPTY;
	}

	public Page getNodesFromBase(final Path base, final String start, final int page, final int pageSize) {
		try {
			List<Node> nodes = new ArrayList<>();
			var startPath = base.resolve(start);
			long total = Files.list(startPath).filter(fileNameFilter).count();
			int skipCount = (page - 1) * pageSize;

			Files.list(startPath)
					.sorted((path1, path2) -> {
						var filename1 = path1.getFileName().toString();
						var filename2 = path2.getFileName().toString();
						if (filename1.equals("index.md")) {
							return -1;
						} else if (filename2.equals("index.md")) {
							return 1;
						}
						return filename1.compareTo(filename2);
					})
					.filter(fileNameFilter)
					.skip(skipCount)
					.limit(pageSize)
					.forEach(path -> {
						var filename = path.getFileName().toString();
						if (filename.endsWith(".md")) {
							filename = filename.substring(0, filename.length() - 3);
						}
						var name = getName(path);
						var md = parse(path);
						if (md.isPresent()) {
							final Node node = new Node(name.isPresent() ? name.get() : filename, getUrl(path), md.get().content());
							nodes.add(node);
						}

					});
			int totalPages = (int) Math.ceil((float)total / pageSize);
			return new Page<Node>(pageSize, totalPages, page, nodes);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return Page.EMPTY;
	}

}
