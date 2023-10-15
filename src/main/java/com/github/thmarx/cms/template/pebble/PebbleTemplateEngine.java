package com.github.thmarx.cms.template.pebble;

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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.MarkdownRenderer;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.Server;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.functions.list.NodeListFunctionBuilder;
import com.github.thmarx.cms.template.functions.navigation.NavigationFunction;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.cache.tag.CaffeineTagCache;
import io.pebbletemplates.pebble.cache.template.CaffeineTemplateCache;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Duration;
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
	final ExtensionManager extensionManager;
	final MarkdownRenderer markdownRenderer;

	final FileSystem fileSystem;
	
	public PebbleTemplateEngine(final FileSystem fileSystem, final ContentParser contentParser, 
			final ExtensionManager extensionManager, final MarkdownRenderer markdownRenderer) {
		this.fileSystem = fileSystem;
		this.templateBase = fileSystem.resolve("templates/");
		this.contentBase = fileSystem.resolve("content/");
		this.contentParser = contentParser;
		this.extensionManager = extensionManager;
		this.markdownRenderer = markdownRenderer;
		var loader = new FileLoader();
		loader.setPrefix(this.templateBase.toString() + File.separatorChar);
		loader.setSuffix(".html");
		final PebbleEngine.Builder builder = new PebbleEngine.Builder()
				.loader(loader);
		
		if (Server.DEV_MODE) {
			builder.templateCache(null);
			builder.tagCache(null);
			builder.cacheActive(false);
		} else {
			var templateCache = new CaffeineTemplateCache(
					Caffeine.newBuilder()
							.expireAfterWrite(Duration.ofMinutes(1))
							.build()
			);
			builder.templateCache(templateCache);
			var tagCache = new CaffeineTagCache(
					Caffeine.newBuilder()
							.expireAfterWrite(Duration.ofMinutes(1))
							.build()
			);
			builder.tagCache(tagCache);
			builder.cacheActive(true);
		}
		
		engine = builder
				.build();
	}

	@Override
	public String render(String template, Model model, RenderContext context) throws IOException {

		Writer writer = new StringWriter();

		PebbleTemplate compiledTemplate = engine.getTemplate(template);

		Map<String, Object> values = new HashMap<>(model.values);
		model.values.put("navigation", new NavigationFunction(this.fileSystem, model.contentFile, contentParser, markdownRenderer));
		model.values.put("nodeList", new NodeListFunctionBuilder(fileSystem, model.contentFile, contentParser, markdownRenderer));
		values.put("renderContext", context);
		
		extensionManager.getRegisterTemplateSupplier().forEach(service -> {
			values.put(service.name(), service.supplier());
		});

		extensionManager.getRegisterTemplateFunctions().forEach(service -> {
			values.put(service.name(), service.function());
		});
		
		compiledTemplate.evaluate(writer, values);

		return writer.toString();

	}

	@Override
	public void invalidateCache() {
		engine.getTemplateCache().invalidateAll();
	}

}
