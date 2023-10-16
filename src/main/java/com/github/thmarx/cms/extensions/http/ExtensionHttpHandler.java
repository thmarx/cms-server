package com.github.thmarx.cms.extensions.http;

/**
 *
 * @author t.marx
 */
public interface ExtensionHttpHandler {
	
	void execute (ExtensionHttpRequest request, ExtensionHttpResponse response);
}
