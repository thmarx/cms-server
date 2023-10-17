package com.github.thmarx.cms.template;

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

import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.RequestContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
public interface TemplateEngine {
	
	public void invalidateCache();

	String render(final String template, final FreemarkerTemplateEngine.Model model, final RequestContext context) throws IOException;
	
	@RequiredArgsConstructor
	public static class Model {
		public final Map<String, Object> values = new HashMap<>();
		public final Path contentFile;
	} 
}
