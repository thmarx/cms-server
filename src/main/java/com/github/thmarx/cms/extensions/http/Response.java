package com.github.thmarx.cms.extensions.http;

import java.nio.charset.Charset;

/**
 *
 * @author t.marx
 */
public interface Response {
	
	public void addHeader (String name, String value);
	
	public void write (String content, Charset charset);
}
