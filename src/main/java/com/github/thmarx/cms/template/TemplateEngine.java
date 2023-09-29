package com.github.thmarx.cms.template;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.template.list.NodeListFunction;
import com.github.thmarx.cms.template.navigation.NavigationFunction;
import freemarker.cache.FileTemplateLoader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.nio.file.Path;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;

public class TemplateEngine {
	
	private final Configuration config;
	
	private final Path contentBase;
	private final ContentParser contentParser;

	public TemplateEngine(final Path templateBase, final Path contentBase, final ContentParser contentParser) {
		
		this.contentBase = contentBase;
		this.contentParser = contentParser;
		
		config = new Configuration(Configuration.VERSION_2_3_32);
		
		try {
			config.setTemplateLoader(new FileTemplateLoader(templateBase.toFile()));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
		config.setDefaultEncoding("UTF-8");
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		config.setLogTemplateExceptions(false);
		config.setWrapUncheckedExceptions(true);
		config.setFallbackOnNullLoopVariable(false);
		
		config.setSharedVariable("indexOf", new IndexOfMethod());
		config.setSharedVariable("upper", new UpperDirective());
	}

	public String render(final String template, final TemplateEngine.Model model, final RenderContext context) throws IOException {
		model.values.put("navigationFunction", new NavigationFunction(contentBase, model.contentFile, contentParser));
		model.values.put("nodeList", new NodeListFunction(contentBase, model.contentFile, contentParser));
		model.values.put("nodeListExcludeIndex", new NodeListFunction(contentBase, model.contentFile, contentParser, true));
		model.values.put("context", context);
		
		StringWriter out = new StringWriter();
		try {
			Template loadedTemplate = config.getTemplate(template);
			
			loadedTemplate.process(model.values, out);

			return out.toString();
		} catch (TemplateException | IOException e) {
			throw new IOException(e);
		} finally {
			out.close();
		}
	}
	
	@RequiredArgsConstructor
	public static class Model {
		public final Map<String, Object> values = new HashMap<>();
		public final Path contentFile;
	} 
}
