package com.github.thmarx.cms;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
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
	
	public String excerpt (final String markdown, final int length) {
		Node document = parser.parse(markdown);
		TextCollectingVisitor textCollectingVisitor = new TextCollectingVisitor();
		String text = textCollectingVisitor.collectAndGetText(document);
		
		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}
}
