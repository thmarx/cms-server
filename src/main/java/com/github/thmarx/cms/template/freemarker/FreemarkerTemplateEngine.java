package com.github.thmarx.cms.template.freemarker;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.functions.list.NodeListFunction;
import com.github.thmarx.cms.template.functions.navigation.NavigationFunction;
import freemarker.cache.FileTemplateLoader;
import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.nio.file.Path;

public class FreemarkerTemplateEngine implements TemplateEngine {
	
	private final Configuration config;
	
	private final Path contentBase;
	private final ContentParser contentParser;

	private final ExtensionManager extensionManager;
	
	public FreemarkerTemplateEngine(final Path templateBase, final Path contentBase, final ContentParser contentParser, final ExtensionManager extensionManager) {
		
		this.contentBase = contentBase;
		this.contentParser = contentParser;
		this.extensionManager = extensionManager;
		
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
		
		extensionManager.getTemplateDirectiveExtensions().forEach(directive -> {
			config.setSharedVariable(directive.name(), directive.directive());
		});
		
		extensionManager.getTemplateMethodExtensions().forEach(method -> {
			config.setSharedVariable(method.name(), method.method());
		});
	}

	@Override
	public String render(final String template, final FreemarkerTemplateEngine.Model model, final RenderContext context) throws IOException {
		model.values.put("navigation", new NavigationFunction(contentBase, model.contentFile, contentParser));
		model.values.put("nodeList", new NodeListFunction(contentBase, model.contentFile, contentParser));
		model.values.put("nodeListExcludeIndex", new NodeListFunction(contentBase, model.contentFile, contentParser, true));
		model.values.put("renderContext", context);
		
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
	
	
}
