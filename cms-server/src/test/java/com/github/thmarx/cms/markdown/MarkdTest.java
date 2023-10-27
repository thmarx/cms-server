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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class MarkdTest {

	Engine engine;
	Context context;
	Source markedSource;
	Source purifySource;

	Value markedFunction;
	
	@BeforeClass
	public void init() throws IOException {
		engine = Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
		context = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.engine(engine).build();
		
		var content = new String(MarkdTest.class.getResourceAsStream("marked.min.js").readAllBytes(), StandardCharsets.UTF_8);
		markedSource = Source.newBuilder("js", content, "marked.mjs").build();
		context.eval(markedSource);
		markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
	}
	@AfterClass
	public void close () {
		context.close();
		engine.close();
	}
	
	@Test(invocationCount = 10)
	public void renderMarkDown () {
		try (var cxt = Context.newBuilder("js")
				.engine(engine)
				.allowHostAccess(HostAccess.ALL)
				.build()) {
			cxt.eval(markedSource);
			
			cxt.getBindings("js").putMember("markdown", "# markd rulezz!");
			Value value = cxt.eval("js", "marked.parse(markdown)");
			System.out.println(value.asString());
		}
	}
	
	@Test(invocationCount = 10)
	public void global () {
		System.out.println(markedFunction.execute("*bold*"));
	}

}
