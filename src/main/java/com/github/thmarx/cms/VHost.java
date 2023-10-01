/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.template.TemplateEngine;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
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
	
	private Properties properties;
	
	public void init() throws IOException {
		
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
		TemplateEngine templates = new TemplateEngine(templateBase, contentBase, contentParser, extensionManager);
		

		contentRenderer = new ContentRenderer(contentParser, templates, new MarkdownRenderer());
		contentResolver = new ContentResolver(contentBase, contentRenderer);
	}

	public HttpHandler httpHandler() {
		final PathResourceManager resourceManager = new PathResourceManager(assetBase);
		ResourceHandler staticResourceHandler = new ResourceHandler(resourceManager);

		ResourceHandler faviconHandler = new ResourceHandler(new FileResourceManager(assetBase.resolve("favicon.ico").toFile()));

		var pathHandler = Handlers.path(new DefaultHttpHandler(contentResolver))
				.addPrefixPath("/assets", staticResourceHandler)
				.addExactPath("/favicon.ico", faviconHandler);
		
		extensionManager.getHttpHandlerExtensions().forEach(handler -> {
			pathHandler.addExactPath(handler.path(), handler.handler());
		});
		
		return pathHandler;
	}
}
