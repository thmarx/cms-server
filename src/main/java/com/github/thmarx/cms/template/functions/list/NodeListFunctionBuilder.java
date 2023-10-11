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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NodeListFunctionBuilder extends AbstractCurrentNodeFunction {

	int page = NodeListFunction.DEFAULT_PAGE;
	int size = NodeListFunction.DEFAUTL_PAGE_SIZE;

	String from = "";

	boolean index = true;

	String sort = "title";
	boolean reverse = false;

	final NodeListFunction nodeListFunction;

	final NodeListFunction nodeListFunctionNoIndex;

	public final Comparator<MetaData.MetaNode> nameComparator = (node1, node2) -> {
		var filename1 = getName(node1);
		var filename2 = getName(node2);
		if (filename1.equals("index.md")) {
			return -1;
		} else if (filename2.equals("index.md")) {
			return 1;
		}
		return filename1.compareTo(filename2);
	};

	public NodeListFunctionBuilder(FileSystem fileSystem, Path currentNode, ContentParser contentParser) {
		super(fileSystem, currentNode, contentParser);
		this.nodeListFunction = new NodeListFunction(fileSystem, currentNode, contentParser);
		this.nodeListFunctionNoIndex = new NodeListFunction(fileSystem, currentNode, contentParser, true);
	}

	public NodeListFunctionBuilder from(String from) {
		this.from = from;
		return this;
	}

	public NodeListFunctionBuilder page(int page) {
		this.page = page;
		return this;
	}

	public NodeListFunctionBuilder size(int size) {
		this.size = size;
		return this;
	}

	public NodeListFunctionBuilder index(boolean index) {
		this.index = index;
		return this;
	}

	public NodeListFunctionBuilder sort(String sort) {
		this.sort = sort;
		return this;
	}

	public NodeListFunctionBuilder reverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public Page<Node> list() {
		NodeListFunction function = nodeListFunction;
		if (!index) {
			function = nodeListFunctionNoIndex;
		}

		Comparator<MetaData.MetaNode> comparator = getComparator();
		if (reverse) {
			comparator = comparator.reversed();
		}

		return function.list(from, page, size, comparator);
	}

	private Comparator<MetaData.MetaNode> getComparator() {
		if (sort == null || "name".equals("sort")) {
			return nameComparator;
		} else {

			return Comparator.comparing(new Function<MetaData.MetaNode, Object>() {
				@Override
				public Object apply(MetaData.MetaNode node) {
					return node.data().get(sort);
				}
			}, Comparator.nullsLast((key1, key2) -> {
				if (Objects.equals(key1, key2)) {
					return 0;
				}
				if (key1 == null && key2 != null) {
					return -1;
				} else if (key1 != null && key2 == null) {
					return 1;
				}
				return ((Comparable)key1).compareTo((Comparable)key2);
			})
			);
		}
	}
}
