/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;


import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class DefaultHttpHandler implements HttpHandler {

	private final ContentResolver contentResolver;
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		RenderContext context = new RenderContext(exchange.getRelativePath(), exchange.getQueryParameters());
		Optional<String> content = contentResolver.getContent(context);
		if (!content.isPresent()) {
			context = new RenderContext("/_technical/404", exchange.getQueryParameters());
			content = contentResolver.getContent(context);
			exchange.setStatusCode(404);
		}
		exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "text/html; charset=utf-8");
		exchange.getResponseSender().send(content.get(), StandardCharsets.UTF_8);
	}

}
