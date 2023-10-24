package com.github.thmarx.cms.template.thymeleaf;

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

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.markdown.FlexMarkMarkdownRenderer;
import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.extensions.ExtensionHolder;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.markdown.MarkdownRenderer;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.TemplateEngineTest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author thmar
 */
public class ThymeleafTemplateEngineNGTest extends TemplateEngineTest {

	TemplateEngine templateEngine;
	private FileSystem fileSystem;
	private MarkdownRenderer markdownRenderer;

	@BeforeClass
	public void setup() {
		fileSystem = new FileSystem(Path.of("./hosts/test_thymeleaf"), new EventBus());
		var contentParser = new ContentParser(fileSystem);

		markdownRenderer = new FlexMarkMarkdownRenderer();
		
		templateEngine = new ThymeleafTemplateEngine(fileSystem, contentParser);
	}

	@Test
	public void testSomeMethod() throws IOException {
		var model = new TemplateEngine.Model(null);
		model.values.put("meta", Map.of("title", "Hello World"));
		model.values.put("content", "The content!");
		var html = templateEngine.render("test.html", model, requestContext());

		var expected = """
                 <!DOCTYPE html>
                 <html>
                 	<head>
                 		<title>Hello World</title>
                 	</head>
                 	<body>The content!</body>
                 </html>
                 """;
		
		Assertions.assertThat(html).isEqualToIgnoringWhitespace(expected);
	}

}
