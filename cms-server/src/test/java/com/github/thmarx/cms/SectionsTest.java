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

import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.template.TemplateEngineTest;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class SectionsTest extends TemplateEngineTest {
	
		ContentRenderer contentRenderer;
	private MarkdownRenderer markdownRenderer;
	private FileSystem fileSystem;
	
	@BeforeClass
	public void beforeClass () throws IOException {
		fileSystem = new FileSystem(Path.of("hosts/test/"), new EventBus());
		fileSystem.init();
		var contentParser = new ContentParser(fileSystem);
		markdownRenderer = TestHelper.getRenderer();
		TemplateEngine templates = new FreemarkerTemplateEngine(fileSystem, contentParser);
		
		contentRenderer = new ContentRenderer(contentParser, templates, fileSystem);
	}
	
	@Test
	public void test_sections() throws IOException {
		List<MetaData.MetaNode> listSections = fileSystem.listSections(fileSystem.resolve("content/page.md"));
		Assertions.assertThat(listSections).hasSize(4);
		
		Map<String, List<ContentRenderer.Section>> renderSections = contentRenderer.renderSections(listSections, requestContext());
		
		Assertions.assertThat(renderSections)
				.hasSize(1)
				.containsKey("left");
		
		Assertions.assertThat(renderSections.get("left"))
				.hasSize(4);
		
		var sectionIndexes = renderSections.get("left").stream().map(section -> section.index()).collect(Collectors.toList());
		Assertions.assertThat(sectionIndexes).containsExactly(0, 1, 2, 10);
	}
}
