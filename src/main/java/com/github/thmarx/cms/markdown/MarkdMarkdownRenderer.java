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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 *
 * @author t.marx
 */
public class MarkdMarkdownRenderer implements MarkdownRenderer {

	public final Context context;
	
	public final Value _markedFunction;
	public final Source markedSource;
	
	public MarkdMarkdownRenderer (final Engine engine) throws IOException {
		context = Context.newBuilder("js")
				.engine(engine)
				.allowHostAccess(HostAccess.ALL)
				.build();
		
		var content = new String(MarkdMarkdownRenderer.class.getResourceAsStream("marked.min.js").readAllBytes(), StandardCharsets.UTF_8);
		markedSource = Source.newBuilder("js", content, "marked.mjs").build();
		context.eval(markedSource);
		_markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
	}
	
	@Override
	public String excerpt(String markdown, int length) {
		try (var context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build()) {
			context.eval(markedSource);
			var markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
			return markedFunction.execute(markdown).asString();
		}
	}

	@Override
	public String render(String markdown) {
		try (var context = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).build()) {
			context.eval(markedSource);
			var markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
			return markedFunction.execute(markdown).asString();
		}
	}
	
}
