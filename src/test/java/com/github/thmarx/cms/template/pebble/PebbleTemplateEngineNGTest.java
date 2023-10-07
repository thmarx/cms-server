/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.template.pebble;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.template.TemplateEngine;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author thmar
 */
public class PebbleTemplateEngineNGTest {
	
	TemplateEngine templateEngine;
	@BeforeClass
	public void setup () {
		final FileSystem fileSystem = new FileSystem(Path.of("hosts/test_pebble/"));
		var contentParser = new ContentParser(fileSystem);
		
		templateEngine = new PebbleTemplateEngine(fileSystem, contentParser);
	}

	@Test
	public void testSomeMethod() throws IOException {
		var model = new TemplateEngine.Model(null);
		model.values.put("title", "Hello World");
		model.values.put("content", "The content!");
		var html = templateEngine.render("test", model, null);
		
		var expected = """
                 <html>
                 	<head>
                 		<title>Hello World</title>
                 	</head>
                 	<body>The content!</body>
                 </html>
                 """;
		
		Assertions.assertThat(html).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void node_list() throws IOException {
		var model = new TemplateEngine.Model(null);
		model.values.put("title", "Hello World");
		var html = templateEngine.render("list", model, null);
		
		var expected = """
                 <html>
                 	<head>
                 		<title>Hello World</title>
                 	</head>
                 	<body></body>
                 </html>
                 """;
		
		Assertions.assertThat(html).isEqualToIgnoringWhitespace(expected);
	}	
}
