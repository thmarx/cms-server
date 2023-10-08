/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author t.marx
 */
public class MetaData {
	
	private ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();
	
	void clear () {
		nodes.clear();
	}
	
	ConcurrentMap<String, Node> nodes () {
		return new ConcurrentHashMap<>(nodes);
	}
	
	public void add(Node node) {
		nodes.put(node.uri, node);
	}
	
	public Optional<Node> byUri(final String uri) {
		if (!nodes.containsKey(uri)) {
			return Optional.empty();
		}
		return Optional.of(nodes.get(uri));
	}
	
	public boolean isVisible (final String uri) {
		
		if (nodes.containsKey(uri)) {
			Node node = nodes.get(uri);
			return !(boolean) node.data().getOrDefault("draft", false);
		}
		
		return false;
	}

	void remove(String uri) {
		nodes.remove(uri);
	}
	
	public static record Node (String uri, Map<String, Object> data){}
}
