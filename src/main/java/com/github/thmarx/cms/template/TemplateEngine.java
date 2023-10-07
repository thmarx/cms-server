/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.github.thmarx.cms.template;

import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.RenderContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
public interface TemplateEngine {

	String render(final String template, final FreemarkerTemplateEngine.Model model, final RenderContext context) throws IOException;
	
	@RequiredArgsConstructor
	public static class Model {
		public final Map<String, Object> values = new HashMap<>();
		public final Path contentFile;
	} 
}
