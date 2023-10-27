package com.github.thmarx.cms.modules.marked;

/*-
 * #%L
 * example-module
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
import com.github.thmarx.cms.api.extensions.MarkdownRendererProviderExtentionPoint;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.modules.api.ModuleLifeCycleExtension;
import com.github.thmarx.modules.api.annotation.Extension;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;

/**
 *
 * @author t.marx
 */
@Extension(ModuleLifeCycleExtension.class)
@Slf4j
public class MarkedRendererLifecycle extends ModuleLifeCycleExtension {

	protected static Engine engine;

	

	@Override
	public void deactivate() {
		log.debug("destroy engine");
		engine.close(true);
	}

	@Override
	public void activate() {
		log.debug("create engine");
		engine = Engine.create();
	}

	@Override
	public void init() {
	}

	
}
