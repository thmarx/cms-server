package com.github.thmarx.cms;

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

import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.eventbus.EventListener;
import com.github.thmarx.cms.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.eventbus.events.TemplateChangedEvent;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.template.pebble.PebbleTemplateEngine;
import com.github.thmarx.cms.template.thymeleaf.ThymeleafTemplateEngine;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class VHost {

	private final FileSystem fileSystem;

	private ContentRenderer contentRenderer;
	private ContentResolver contentResolver;
	private ContentParser contentParser;
    private TemplateEngine templateEngine;
	private ExtensionManager extensionManager;
	private MarkdownRenderer markdownRenderer;
	
	private Path contentBase;
	private Path assetBase;
	private Path templateBase;

	@Getter
	private String hostname;
	
	@Getter
	private final EventBus eventBus;
	
	private Properties properties;

	public VHost(final Path hostBase) {
		this.eventBus = new EventBus();
		this.fileSystem = new FileSystem(hostBase, eventBus);
	}
	
	public void shutdown ()  {
		try {
			fileSystem.shutdown();
			extensionManager.close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}
	
	public void init() throws IOException {
		
		fileSystem.init();
		
		var props = fileSystem.resolve("host.properties");
		properties = new Properties();
		try (var reader = Files.newBufferedReader(props)) {
			properties.load(reader);
		}
		hostname = properties.getProperty("hostname");
		
		contentBase = fileSystem.resolve("content/");
		assetBase = fileSystem.resolve("assets/");
		templateBase = fileSystem.resolve("templates/");

		extensionManager = new ExtensionManager(fileSystem);
		extensionManager.init();
		
		contentParser = new ContentParser(fileSystem);
		markdownRenderer = new MarkdownRenderer();
		
		templateEngine = resolveTemplateEngine();
		
		contentRenderer = new ContentRenderer(contentParser, templateEngine, markdownRenderer, fileSystem);
		contentResolver = new ContentResolver(contentBase, contentRenderer, fileSystem);
		
		eventBus.register(ContentChangedEvent.class, (EventListener<ContentChangedEvent>) (ContentChangedEvent event) -> {
			log.debug("invalidate content cache");
			contentParser.clearCache();
		});
        eventBus.register(TemplateChangedEvent.class, (EventListener<TemplateChangedEvent>) (TemplateChangedEvent event) -> {
			log.debug("invalidate template cache");
			templateEngine.invalidateCache();
		});
	}
	
	private TemplateEngine resolveTemplateEngine () {
		var engine = this.properties.getProperty("template.engine", "freemarker");
		return switch (engine) {
			case "thymeleaf" -> new ThymeleafTemplateEngine(fileSystem, contentParser, extensionManager, markdownRenderer);
			case "pebble" -> new PebbleTemplateEngine(fileSystem, contentParser, markdownRenderer);
			default -> new FreemarkerTemplateEngine(fileSystem, contentParser, extensionManager, markdownRenderer);
		};
	}

	public HttpHandler httpHandler() {
		final PathResourceManager resourceManager = new PathResourceManager(assetBase);
		ResourceHandler staticResourceHandler = new ResourceHandler(resourceManager);

		ResourceHandler faviconHandler = new ResourceHandler(new FileResourceManager(assetBase.resolve("favicon.ico").toFile()));

		var pathHandler = Handlers.path(new DefaultHttpHandler(contentResolver))
				.addPrefixPath("/assets", staticResourceHandler)
				.addExactPath("/favicon.ico", faviconHandler);
		
		extensionManager.getHttpHandlerExtensions().forEach(handler -> {
			pathHandler.addExactPath(handler.path(), new BlockingHandler(handler.handler()));
		});
		
		return pathHandler;
	}
}
