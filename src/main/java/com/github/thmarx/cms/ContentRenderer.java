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

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.template.TemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ContentRenderer {

	private final ContentParser contentParser;
	private final TemplateEngine templates;
	private final MarkdownRenderer markdownRenderer;
	private final FileSystem fileSystem;
	
	public String render (final Path contentFile, final RenderContext context) throws IOException {
		return render(contentFile, context, Collections.emptyMap());
	}
	
	public String render (final Path contentFile, final RenderContext context, Map<String, Section> sections) throws IOException {
		var content = contentParser.parse(contentFile);
		
		TemplateEngine.Model model = new TemplateEngine.Model(contentFile);
		model.values.putAll(content.meta());
		model.values.put("content", markdownRenderer.render(content.content()));
		model.values.put("sections", sections);
		return templates.render((String)content.meta().get("template"), model, context);
	}
	
	public Map<String, Section> renderSections (final List<MetaData.MetaNode> sectionNodes, final RenderContext context) throws IOException {
		
		if (sectionNodes.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, Section> sections = new HashMap<>();

		final Path contentBase = fileSystem.resolve("content/");
		sectionNodes.forEach(node -> {
			try {
				var sectionPath = contentBase.resolve(node.uri());
				var content = render(sectionPath, context);
				final Matcher matcher = Constants.SECTION_PATTERN.matcher(node.name());
				matcher.matches();
				var name = matcher.group("section");
				
				sections.put(name, new Section(name, content));
			} catch (Exception ex) {
				log.error("error render section", ex);
			}
			
		});
		
		return sections;
	}

	public static record Section (String name, String content) {}
}
