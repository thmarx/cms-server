package com.github.thmarx.cms.extensions.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.Charset;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class UndertowResponse implements Response {
	
	private final HttpServerExchange exchange;

	@Override
	public void addHeader(String name, String value) {
		exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
	}

	@Override
	public void write(String content, Charset charset) {
		exchange.getResponseSender().send(content, charset);
	}
}
