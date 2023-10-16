package com.github.thmarx.cms.extensions.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ExtensionHttpHandlerWrapper implements HttpHandler {
	
	private final ExtensionHttpHandler handler;
	
	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		handler.execute(new UndertowRequest(exchange), new UndertowResponse(exchange));
	}
	
}
