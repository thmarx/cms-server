/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

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

import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.extensions.ExtensionFileSystem;
import com.github.thmarx.cms.markdown.MarkedMarkdownRenderer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.IOAccess;

/**
 *
 * @author t.marx
 */
public abstract class TestHelper {

	static Engine engine;

	static {
		engine = Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
	}

	static MarkdownRenderer renderer;

	public static MarkdownRenderer getRenderer() {
		if (renderer == null) {
			var context = Context.newBuilder()
					.allowAllAccess(true)
					.allowHostClassLookup(className -> true)
					.allowHostAccess(HostAccess.ALL)
					.allowValueSharing(true)
					.engine(engine).build();
			renderer = new MarkedMarkdownRenderer(context);
		}

		return renderer;
	}

}
