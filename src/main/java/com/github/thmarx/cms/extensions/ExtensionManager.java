/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.extensions;

import com.github.thmarx.cms.filesystem.FileSystem;
import io.undertow.server.HttpHandler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.polyglot.proxy.ProxyObject;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ExtensionManager implements AutoCloseable {

	private final FileSystem fileSystem;

	private Engine engine;

	@Getter
	private final List<HttpHandlerExtension> httpHandlerExtensions = new ArrayList<>();
	@Getter
	private final List<TemplateSupplierExtension> registerTemplateSupplier = new ArrayList<>();
	@Getter
	private final List<TemplateFunctionExtension> registerTemplateFunctions = new ArrayList<>();
	private Context context;

	public void registerHttpExtension(final String path, final HttpHandler handler) {
		httpHandlerExtensions.add(new HttpHandlerExtension(path, handler));
	}

	public void registerTemplateSupplier(final String path, final Supplier<?> supplier) {
		registerTemplateSupplier.add(new TemplateSupplierExtension(path, supplier));
	}

	public void registerTemplateFunction(final String path, final Function<?, ?> function) {
		registerTemplateFunctions.add(new TemplateFunctionExtension(path, function));
	}

	private ClassLoader getClassLoader() throws IOException {
		Path libs = fileSystem.resolve("libs/");
		List<URL> urls = new ArrayList<>();
		if (Files.exists(libs)) {
			Files.list(libs)
					.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.forEach(path -> {
						try {
							urls.add(path.toUri().toURL());
						} catch (MalformedURLException ex) {
							log.error("", ex);
						}
					});
		}
		return new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getSystemClassLoader());
	}

	public void init() throws IOException {
		if (engine == null) {
            log.debug("init extensions");
			engine = Engine.newBuilder("js")
					.option("engine.WarnInterpreterOnly", "false")
					.build();
			context = Context.newBuilder()
					.allowAllAccess(true)
					.allowHostClassLookup(className -> true)
					.allowHostAccess(HostAccess.ALL)
					.hostClassLoader(getClassLoader())
					.allowIO(IOAccess.newBuilder()
							.fileSystem(new ExtensionFileSystem(fileSystem.resolve("extensions/")))
							.build())
					.engine(engine).build();

			var extPath = fileSystem.resolve("extensions/");
			if (Files.exists(extPath)) {
                log.debug("try to find extensions");
				Files.list(extPath)
						.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".js"))
						.forEach(extFile -> {
							try {
                                log.debug("load extension {}", extFile.getFileName().toString());
								context.getBindings("js").putMember("extensions", this);
								Source source = Source.newBuilder(
										"js",
										Files.readString(extFile, StandardCharsets.UTF_8),
										extFile.getFileName().toString() + ".mjs")
										.encoding(StandardCharsets.UTF_8)
										.build();
								context.eval(source);
							} catch (IOException ex) {
								log.error("", ex);
							}
						});
			}

		}
	}

	@Override
	public void close() throws Exception {
		if (engine != null) {
			context.close(true);
			engine.close(true);
		}
	}

}
