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
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class TestTemplateEngine implements TemplateEngine {

	private StringSubstitutor stringSubstitutor = new StringSubstitutor();

	private final FileSystem fileSystem;
	
	@Override
	public void invalidateCache() {

	}

	@Override
	public String render(final String template, Model model) throws IOException {
		
		Map<String, Object> values = new HashMap<>();
		values.putAll(model.values);
		values.put("meta.title", ((Map<String, Object>)model.values.getOrDefault("meta", Map.of())).getOrDefault("title", "<no title>"));
		
		String templateContent = fileSystem.loadContent(fileSystem.resolve("templates").resolve(template), StandardCharsets.UTF_8);
		
		StringSubstitutor sub = new StringSubstitutor(values);
		return sub.replace(templateContent);
	}

}
