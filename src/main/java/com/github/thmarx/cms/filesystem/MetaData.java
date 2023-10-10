/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.Constants;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class MetaData {

	private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();

	private ConcurrentMap<String, Node> tree = new ConcurrentHashMap<>();

	void clear() {
		nodes.clear();
	}

	ConcurrentMap<String, Node> nodes() {
		return new ConcurrentHashMap<>(nodes);
	}

	ConcurrentMap<String, Node> tree() {
		return new ConcurrentHashMap<>(tree);
	}

	public void createDirectory(final String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			return;
		}
		String pattern = Pattern.quote("/");
		var parts = uri.split(pattern);
		Node n = new Node(uri, parts[parts.length - 1], Map.of(), true);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children.put(n.name(), n);
		} else {
			tree.put(n.name(), n);
		}
	}

	private Optional<Node> getFolder(String uri) {
		String pattern = Pattern.quote("/");
		var parts = uri.split(pattern);

		final AtomicReference<Node> folder = new AtomicReference<>(null);
		Stream.of(parts).forEach(part -> {
			if (part.endsWith(".md")) {
				return;
			}
			if (folder.get() == null) {
				folder.set(tree.get(part));
			} else {
				folder.set(folder.get().children.get(part));
			}
		});
		return Optional.ofNullable(folder.get());
	}

	public void addFile(final String uri, final Map<String, Object> data) {
		String pattern = Pattern.quote("/");
		var parts = uri.split(pattern);
		final Node node = new Node(uri, parts[parts.length - 1], data);

		nodes.put(uri, node);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children.put(node.name(), node);
		} else {
			tree.put(node.name(), node);
		}
	}

	public Optional<Node> byUri(final String uri) {
		if (!nodes.containsKey(uri)) {
			return Optional.empty();
		}
		return Optional.of(nodes.get(uri));
	}

	void remove(String uri) {
		nodes.remove(uri);
		
		var folder = getFolder(uri);
		String pattern = Pattern.quote("/");
		var parts = uri.split(pattern);
		var name = parts[parts.length - 1];
		if (folder.isPresent()) {
			folder.get().children.remove(name);
		} else {
			tree.remove(name);
		}
	}

	public static record Node(String uri, String name, Map<String, Object> data, boolean isDirectory, Map<String, Node> children) {

		public Node(String uri, String name, Map<String, Object> data, boolean isDirectory) {
			this(uri, name, data, isDirectory, new HashMap<String, Node>());
		}

		public Node(String uri, String name, Map<String, Object> data) {
			this(uri, name, data, false, new HashMap<String, Node>());
		}
		
		public boolean isHidden () {
			return name.startsWith(".");
		}
		
		public boolean isDraft () {
			return (boolean) data().getOrDefault("draft", false);
		}
		
		public boolean isPublished () {
		var localDate = (LocalDate)data.getOrDefault("published", LocalDate.now());
			var now = LocalDate.now();
			return !isDraft() && (localDate.isBefore(now) || localDate.isEqual(now));
		}
		
		public boolean isSection () {
			return Constants.SECTION_PATTERN.matcher(name).matches();
		}
	}
}
