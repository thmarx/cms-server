package com.github.thmarx.cms.template.freemarker;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.filesystem.FileSystem;
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
import freemarker.template.TemplateModelException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FreemarkerTemplateEngine implements TemplateEngine {
	
	private final Configuration config;
	
	private final Path contentBase;
	private final ContentParser contentParser;

	private final ExtensionManager extensionManager;
	
	final FileSystem fileSystem;
	public FreemarkerTemplateEngine(final FileSystem fileSystem, final ContentParser contentParser, final ExtensionManager extensionManager) {
		this.fileSystem = fileSystem;
		this.contentBase = fileSystem.resolve("content/");
		this.contentParser = contentParser;
		this.extensionManager = extensionManager;
		
		config = new Configuration(Configuration.VERSION_2_3_32);
		
		try {
			config.setTemplateLoader(new FileTemplateLoader(fileSystem.resolve("templates/").toFile()));
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
		
		extensionManager.getRegisterTemplateSupplier().forEach(service -> {
			try {
				config.setSharedVariable(service.name(), service.supplier());
			} catch (TemplateModelException ex) {
				log.error(null, ex);
			}
		});
		
		extensionManager.getRegisterTemplateFunctions().forEach(service -> {
			try {
				config.setSharedVariable(service.name(), service.function());
			} catch (TemplateModelException ex) {
				log.error(null, ex);
			}
		});
	}

	@Override
	public String render(final String template, final FreemarkerTemplateEngine.Model model, final RenderContext context) throws IOException {
		model.values.put("navigation", new NavigationFunction(this.fileSystem, model.contentFile, contentParser));
		model.values.put("nodeList", new NodeListFunction(fileSystem, model.contentFile, contentParser));
		model.values.put("nodeListExcludeIndex", new NodeListFunction(fileSystem, model.contentFile, contentParser, true));
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
