/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.template.thymeleaf;

import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.template.TemplateEngine;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Locale;
import org.thymeleaf.Thymeleaf;
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

	public ThymeleafTemplateEngine(final Path templateBase) {
		this.templateBase = templateBase;
		
		var templateResolver = new FileTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setPrefix(this.templateBase.toString() + File.separatorChar);
		templateResolver.setSuffix(".html");
		engine = new org.thymeleaf.TemplateEngine();
		engine.setTemplateResolver(templateResolver);
	}

	@Override
	public String render(String template, TemplateEngine.Model model, RenderContext context) throws IOException {

		Writer writer = new StringWriter();
		engine.process(template, new Context(Locale.getDefault(), model.values), writer);
		return writer.toString();
	}

}
