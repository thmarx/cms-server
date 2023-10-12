/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.filesystem.FileSystem;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class Server {

	public static boolean DEV_MODE = false;
	
	public static void main(String[] args) throws Exception {

		System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
		System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");

		Properties properties = new Properties();
		try (var inStream = new FileInputStream("application.properties")) {
			properties.load(inStream);
		}
		DEV_MODE = Boolean.parseBoolean(properties.getProperty("dev", "false"));

		List<VHost> vhosts = new ArrayList<>();
		Files.list(Path.of("hosts")).forEach((hostPath) -> {
			var props = hostPath.resolve("host.properties");
			if (Files.exists(props)) {
				try {
					VHost host = new VHost(hostPath);
					host.init();
					vhosts.add(host);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		var hostHandlers = Handlers.virtualHost();
		vhosts.forEach(host -> {
			log.debug("add virtual host : " + host.getHostname());
			hostHandlers.addHost(host.getHostname(), host.httpHandler());
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			log.debug("shutting down");

			vhosts.forEach(host -> {
				log.debug("shutting down vhost : " + host.getHostname());
				host.shutdown();
			});
		}));

		Undertow server = Undertow.builder()
				.addHttpListener(Integer.valueOf(properties.getProperty("server.port", "8080")), "0.0.0.0")
				.setHandler(hostHandlers)
				.setServerOption(UndertowOptions.URL_CHARSET, "UTF8")
				.build();
		server.start();

	}

}
