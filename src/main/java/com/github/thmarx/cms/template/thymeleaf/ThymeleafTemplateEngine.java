/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.thymeleaf;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.Server;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.functions.list.NodeListFunctionBuilder;
import com.github.thmarx.cms.template.functions.navigation.NavigationFunction;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 *
 * @author thmar
 */
public class ThymeleafTemplateEngine implements TemplateEngine {

	private final org.thymeleaf.TemplateEngine engine;
	private final Path templateBase;
	private final FileSystem fileSystem;
	private final ContentParser contentParser;

	public ThymeleafTemplateEngine(final FileSystem fileSystem, final ContentParser contentParser) {
		this.fileSystem = fileSystem;
		this.templateBase = fileSystem.resolve("templates/");
		this.contentParser = contentParser;
		
		var templateResolver = new FileTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setPrefix(this.templateBase.toString() + File.separatorChar);
		templateResolver.setSuffix(".html");
		if (Server.DEV_MODE) {
			templateResolver.setCacheable(false);
		} else {
			templateResolver.setCacheable(true);
			templateResolver.setCacheTTLMs(TimeUnit.MINUTES.toMillis(1));
		}
		
		engine = new org.thymeleaf.TemplateEngine();
		engine.setTemplateResolver(templateResolver);
	}

	@Override
	public String render(String template, TemplateEngine.Model model, RenderContext context) throws IOException {

		Writer writer = new StringWriter();
		
		Map<String, Object> values = new HashMap<>(model.values);
		values.put("navigation", new NavigationFunction(this.fileSystem, model.contentFile, contentParser));
		values.put("nodeList", new NodeListFunctionBuilder(fileSystem, model.contentFile, contentParser));
		values.put("renderContext", context);
		
		engine.process(template, new Context(Locale.getDefault(), values), writer);
		return writer.toString();
	}

	@Override
	public void invalidateCache() {
		engine.getCacheManager().clearAllCaches();
	}

}
