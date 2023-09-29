/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.template.TemplateEngine;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author t.marx
 */
public class Server {

	static ContentRenderer contentRenderer;
	static ContentResolver contentResolver;

	private static final Path contentBase = Path.of("content/");
	private static final Path staticBase = Path.of("assets/");
	private static final Path templateBase = Path.of("templates/");

	public static void main(String[] args) throws Exception {

		Properties properties = new Properties();
		try (var inStream = new FileInputStream("application.properties")){
			properties.load(inStream);
		}
		
		TemplateEngine templates = new TemplateEngine(templateBase, contentBase);
		ContentParser parser = new ContentParser(Path.of("content/"));

		contentRenderer = new ContentRenderer(parser, templates, new MarkdownRenderer());
		contentResolver = new ContentResolver(contentBase, contentRenderer);

		final PathResourceManager resourceManager = new PathResourceManager(staticBase);
		ResourceHandler staticResourceHandler = new ResourceHandler(resourceManager);

		ResourceHandler faviconHandler = new ResourceHandler(new FileResourceManager(staticBase.resolve("favicon.ico").toFile()));
		
		
		
		Undertow server = Undertow.builder()
				.addHttpListener(Integer.valueOf(properties.getProperty("server.port", "8080")), "0.0.0.0")
				.setHandler(Handlers.path(new DefaultHttpHandler(contentResolver))
						.addPrefixPath("/assets", staticResourceHandler)
						.addExactPath("/favicon.ico", faviconHandler))
				.build();
		server.start();
	}
	
}
