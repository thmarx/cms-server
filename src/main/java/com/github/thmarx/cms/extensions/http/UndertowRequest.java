package com.github.thmarx.cms.extensions.http;

import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class UndertowRequest implements Request {

	private final HttpServerExchange exchange;

	private String body = "";
	public boolean bodyRead = false;
	
	public String getBody () {
		return getBody(StandardCharsets.UTF_8);
	}
	
	public String getBody (final Charset charset) {
		if (!bodyRead) {			
			try {
				body = new String(exchange.getInputStream().readAllBytes(), charset);
				bodyRead = true;
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
		return body;
	}
	
	public List<String> getQueryParamter(final String name) {
		if (exchange.getQueryParameters().containsKey(name)) {
			return new ArrayList<>(exchange.getQueryParameters().get(name));
		}
		return Collections.emptyList();
	}

	public List<String> getQueryParamters() {
		return new ArrayList<>(exchange.getQueryParameters().keySet());
	}
}
