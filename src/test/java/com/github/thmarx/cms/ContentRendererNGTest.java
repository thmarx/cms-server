/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.ContentRenderer;
import com.github.thmarx.cms.MarkdownRenderer;
import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.template.TemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import static org.testng.Assert.*;
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
		TemplateEngine templates = new TemplateEngine(Path.of("templates/"), Path.of("content/"));
		ContentParser parser = new ContentParser(Path.of("content/"));
		
		contentRenderer = new ContentRenderer(parser, templates, new MarkdownRenderer());
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
		var content = contentRenderer.render(Path.of("content/test.md"), context);
		
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
		var content = contentRenderer.render(Path.of("content/products/test.md"), context);
		
		Assertions.assertThat(content).isEqualToIgnoringWhitespace(expectedHTML);
	}
}
