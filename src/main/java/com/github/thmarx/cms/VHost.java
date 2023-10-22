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
import com.github.thmarx.cms.markdown.FlexMarkMarkdownRenderer;
import com.github.thmarx.cms.markdown.MarkdMarkdownRenderer;
import com.github.thmarx.cms.markdown.MarkdownRenderer;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.template.pebble.PebbleTemplateEngine;
import com.github.thmarx.cms.template.thymeleaf.ThymeleafTemplateEngine;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.cache.CacheHandler;
import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;

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

	private Path contentBase;
	private Path assetBase;
	private Path templateBase;

	@Getter
	private String hostname;

	@Getter
	private final EventBus eventBus;

	private HostProperties properties;

	public VHost(final Path hostBase) {
		this.eventBus = new EventBus();
		this.fileSystem = new FileSystem(hostBase, eventBus);
	}

	public void shutdown() {
		try {
			fileSystem.shutdown();
			extensionManager.close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	public void init() throws IOException {

		fileSystem.init();

		var props = fileSystem.resolve("site.yaml");
		properties = new HostProperties().load(props);

		hostname = properties.hostname();

		contentBase = fileSystem.resolve("content/");
		assetBase = fileSystem.resolve("assets/");
		templateBase = fileSystem.resolve("templates/");

		extensionManager = new ExtensionManager(fileSystem);
		extensionManager.init();

		contentParser = new ContentParser(fileSystem);

		templateEngine = resolveTemplateEngine();

		contentRenderer = new ContentRenderer(contentParser, templateEngine, fileSystem);
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

	private TemplateEngine resolveTemplateEngine() {
		var engine = this.properties.templateEngine();
		return switch (engine) {
			case "thymeleaf" ->
				new ThymeleafTemplateEngine(fileSystem, contentParser);
			case "pebble" ->
				new PebbleTemplateEngine(fileSystem, contentParser);
			default ->
				new FreemarkerTemplateEngine(fileSystem, contentParser);
		};
	}

	private MarkdownRenderer resolveMarkdownRenderer(final Context context) {
		var engine = this.properties.markdownEngine();
		return switch (engine) {
			case "flexmark" ->
				new FlexMarkMarkdownRenderer();
			case "markd" ->
				new MarkdMarkdownRenderer(context);
			default ->
				new FlexMarkMarkdownRenderer();
		};
	}

	public HttpHandler httpHandler() {
		final PathResourceManager resourceManager = new PathResourceManager(assetBase);
		ResourceHandler staticResourceHandler = new ResourceHandler(resourceManager);
		// TODO: think about some better strategy for ttl
		if (!Server.DEV_MODE) {
			staticResourceHandler.setCacheTime((int)TimeUnit.DAYS.toSeconds(1));
		}
		HttpHandler compressionHandler = new EncodingHandler.Builder().build(null).wrap(staticResourceHandler);
		//DirectBufferCache assetCache = new DirectBufferCache(100, 10, 1000);
		//CacheHandler cacheHandler = new CacheHandler(assetCache, new EncodingHandler.Builder().build(null).wrap(staticResourceHandler));
		
		ResourceHandler faviconHandler = new ResourceHandler(new FileResourceManager(assetBase.resolve("favicon.ico").toFile()));
		
		var pathHandler = Handlers.path(new DefaultHttpHandler(contentResolver, extensionManager, (context) -> {
			return resolveMarkdownRenderer(context);
		}))
				.addPrefixPath("/assets", compressionHandler)
				.addExactPath("/favicon.ico", faviconHandler);

		RoutingHandler extensionHandler = Handlers.routing();
		extensionHandler.get("/{name}", new ExtensionsHttpHandler(extensionManager, "get"));
		extensionHandler.post("/{name}", new ExtensionsHttpHandler(extensionManager, "post"));

		pathHandler.addPrefixPath("/extensions", extensionHandler);

		return pathHandler;
	}
}
