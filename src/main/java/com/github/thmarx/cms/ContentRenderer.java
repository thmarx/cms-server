/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.template.TemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ContentRenderer {

	private final ContentParser contentParser;
	private final TemplateEngine templates;
	private final MarkdownRenderer markdownRenderer;
	
	
	public String render (final Path contentFile, final RenderContext context) throws IOException {
		var content = contentParser.parse(contentFile);
		
		TemplateEngine.Model model = new TemplateEngine.Model(contentFile);
		model.values.putAll(content.meta());
		model.values.put("content", markdownRenderer.render(content.content()));
		return templates.render((String)content.meta().get("template"), model, context);
	}
}
