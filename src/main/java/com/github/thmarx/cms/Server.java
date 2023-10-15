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
		//System.setProperty("polyglotimpl.DisableClassPathIsolation", "true");

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
