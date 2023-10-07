/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.template.thymeleaf;

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
public class ThymeleafTemplateEngineNGTest {

	TemplateEngine templateEngine;

	@BeforeClass
	public void setup() {
		var templateBase = Path.of("./hosts/test_thymeleaf/templates");

		templateEngine = new ThymeleafTemplateEngine(templateBase);
	}

	@Test
	public void testSomeMethod() throws IOException {
		var model = new TemplateEngine.Model(null);
		model.values.put("title", "Hello World");
		model.values.put("content", "The content!");
		var html = templateEngine.render("test", model, null);

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
