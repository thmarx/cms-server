/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.eventbus.Event;
import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.eventbus.EventListener;
import com.github.thmarx.cms.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.template.pebble.PebbleTemplateEngine;
import com.github.thmarx.cms.template.thymeleaf.ThymeleafTemplateEngine;
import com.github.thmarx.cms.utils.DateUtil;
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

	private ExtensionManager extensionManager;
	
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
		
		if (properties.containsKey("date.format")) {
			DateUtil.setDateFormat(properties.getProperty("date.format"));
		}
		
		contentBase = fileSystem.resolve("content/");
		assetBase = fileSystem.resolve("assets/");
		templateBase = fileSystem.resolve("templates/");

		extensionManager = new ExtensionManager(fileSystem);
		extensionManager.init();
		
		contentParser = new ContentParser(fileSystem);
		TemplateEngine templates = resolveTemplateEngine();
		

		contentRenderer = new ContentRenderer(contentParser, templates, new MarkdownRenderer(), fileSystem);
		contentResolver = new ContentResolver(contentBase, contentRenderer, fileSystem);
		
		eventBus.register(ContentChangedEvent.class, (EventListener<ContentChangedEvent>) (ContentChangedEvent event) -> {
			log.debug("invalidate ContentParser cache");
			contentParser.clearCache();
		});
	}
	
	private TemplateEngine resolveTemplateEngine () {
		var engine = this.properties.getProperty("template.engine", "freemarker");
		return switch (engine) {
			case "thymeleaf" -> new ThymeleafTemplateEngine(fileSystem, contentParser);
			case "pebble" -> new PebbleTemplateEngine(fileSystem, contentParser);
			default -> new FreemarkerTemplateEngine(fileSystem, contentParser, extensionManager);
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
