package com.github.thmarx.cms.template.thymeleaf;

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
import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.MarkdownRenderer;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.Server;
import com.github.thmarx.cms.extensions.ExtensionManager;
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
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 *
 * @author thmar
 */
@Slf4j
public class ThymeleafTemplateEngine implements TemplateEngine {

	private final org.thymeleaf.TemplateEngine engine;
	private final Path templateBase;
	private final FileSystem fileSystem;
	private final ContentParser contentParser;
	final MarkdownRenderer markdownRenderer;
	final ExtensionManager extensionManager;

	public ThymeleafTemplateEngine(final FileSystem fileSystem, final ContentParser contentParser,
			final ExtensionManager extensionManager, final MarkdownRenderer markdownRenderer) {
		this.fileSystem = fileSystem;
		this.templateBase = fileSystem.resolve("templates/");
		this.contentParser = contentParser;
		this.markdownRenderer = markdownRenderer;
		this.extensionManager = extensionManager;

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
		values.put("navigation", new NavigationFunction(this.fileSystem, model.contentFile, contentParser, markdownRenderer));
		values.put("nodeList", new NodeListFunctionBuilder(fileSystem, model.contentFile, contentParser, markdownRenderer));
		values.put("renderContext", context);

		extensionManager.getRegisterTemplateSupplier().forEach(service -> {
			values.put(service.name(), service.supplier());
		});

		extensionManager.getRegisterTemplateFunctions().forEach(service -> {
			values.put(service.name(), service.function());
		});

		engine.process(template, new Context(Locale.getDefault(), values), writer);
		return writer.toString();
	}

	@Override
	public void invalidateCache() {
		engine.getCacheManager().clearAllCaches();
	}

}
