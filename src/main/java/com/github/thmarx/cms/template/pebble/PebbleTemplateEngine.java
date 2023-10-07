/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.pebble;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.functions.list.NodeListFunction;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class PebbleTemplateEngine implements TemplateEngine {

	private final PebbleEngine engine;
	private final Path templateBase;
	final Path contentBase; 
	final ContentParser contentParser;

	public PebbleTemplateEngine(final Path templateBase, final Path contentBase, final ContentParser contentParser) {
		this.templateBase = templateBase;
		this.contentBase = contentBase;
		this.contentParser = contentParser;
		var loader = new FileLoader();
		loader.setPrefix(this.templateBase.toString() + File.separatorChar);
		loader.setSuffix(".html");
		engine = new PebbleEngine.Builder()
				.loader(loader)
				.build();
	}

	@Override
	public String render(String template, Model model, RenderContext context) throws IOException {

		Writer writer = new StringWriter();

		PebbleTemplate compiledTemplate = engine.getTemplate(template);

		Map<String, Object> values = new HashMap<>(model.values);
		
		values.put("nodeList", new NodeListFunction(contentBase, model.contentFile, contentParser));
		values.put("renderContext", context);
		
		compiledTemplate.evaluate(writer, values);

		return writer.toString();

	}

}
