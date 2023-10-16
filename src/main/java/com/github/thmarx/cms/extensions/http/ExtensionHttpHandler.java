package com.github.thmarx.cms.extensions.http;

/**
 *
 * @author t.marx
 */
public interface ExtensionHttpHandler {
	
	void execute (Request request, Response response);
}
