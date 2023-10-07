/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class MarkdownRenderer {

	final MutableDataSet options = new MutableDataSet();
	private final Parser parser;
	private final HtmlRenderer renderer;
	
	public MarkdownRenderer () {
		options.set(Parser.EXTENSIONS, List.of(
		));
		parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
	}
	
	public String render (final String markdown) {
		Node document = parser.parse(markdown);
        return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
	}
}
