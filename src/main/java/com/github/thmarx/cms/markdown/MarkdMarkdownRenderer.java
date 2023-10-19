package com.github.thmarx.cms.markdown;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jsoup.Jsoup;

/**
 *
 * @author t.marx
 */
@Slf4j
public class MarkdMarkdownRenderer implements MarkdownRenderer {

	public final Context context;
	
	public final Value markedFunction;
	public final Source markedSource;
	
	public MarkdMarkdownRenderer (final Context context) {
		try {
			this.context = context;
			
			var content = new String(MarkdMarkdownRenderer.class.getResourceAsStream("marked.min.js").readAllBytes(), StandardCharsets.UTF_8);
			markedSource = Source.newBuilder("js", content, "marked.mjs").build();
			context.eval(markedSource);
			markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
		} catch (IOException ex) {
			log.error(null, ex);
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public String excerpt(String markdown, int length) {
		var content = markedFunction.execute(markdown).asString();
		String text = Jsoup.parse(content).text();
		
		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}

	@Override
	public String render(String markdown) {
		return markedFunction.execute(markdown).asString();
	}
	
}
