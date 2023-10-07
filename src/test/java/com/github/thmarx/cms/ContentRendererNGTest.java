/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.template.TemplateEngine;
import com.github.thmarx.cms.template.freemarker.FreemarkerTemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class ContentRendererNGTest {
	
	ContentRenderer contentRenderer;
	
	@BeforeClass
	public void beforeClass () throws IOException {
		final FileSystem fileSystem = new FileSystem(Path.of("hosts/test/"));
		var contentParser = new ContentParser(fileSystem);
		TemplateEngine templates = new FreemarkerTemplateEngine(Path.of("hosts/test/templates/"), Path.of("hosts/test/content/"), contentParser, new ExtensionManager(fileSystem));
		
		contentRenderer = new ContentRenderer(contentParser, templates, new MarkdownRenderer());
	}

	@Test
	public void testSomeMethod() throws IOException {
		var expectedHTML = """
                     <html>
                     	<head>
                     		<title>Startseite</title>
                     	</head>
						<body>
                            <p>Und hier der Inhalt</p>
						</body>
                     </html>
                     """;
		RenderContext context = new RenderContext("/info", Map.of());
		var content = contentRenderer.render(Path.of("hosts/test/content/test.md"), context);
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
	
	@Test
	public void test_subfolder() throws IOException {
		var expectedHTML = """
                     <html>
                     	<head>
                     		<title>ProduktSeite</title>
                     	</head>
						<body>
							<p>Das ist ein Produkt!</p>
                     	</body>
                     </html>
                     """;
		RenderContext context = new RenderContext("/info", Map.of());
		var content = contentRenderer.render(Path.of("hosts/test/content/products/test.md"), context);
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
}
