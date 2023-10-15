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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultHttpHandler implements HttpHandler {

	private final ContentResolver contentResolver;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try {
			RenderContext context = new RenderContext(exchange.getRelativePath(), exchange.getQueryParameters());
			Optional<String> content = contentResolver.getContent(context);
			if (!content.isPresent()) {
				context = new RenderContext("/.technical/404", exchange.getQueryParameters());
				content = contentResolver.getContent(context);
				exchange.setStatusCode(404);
			}
			exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "text/html; charset=utf-8");
			exchange.getResponseSender().send(content.get(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("", e);
			exchange.setStatusCode(500);
			exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "text/html; charset=utf-8");
		}
	}

}
