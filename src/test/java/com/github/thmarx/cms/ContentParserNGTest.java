/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.Assertions;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class ContentParserNGTest {
	
	public ContentParserNGTest() {
	}

	@Test
	public void testSomeMethod() throws IOException {
		var contentParser = new ContentParser(new FileSystem(Path.of("hosts/test/"), new EventBus()));
		
		var expectedMD = """
                   
                   Und hier der Inhalt
                   """;
		
		var content = contentParser.parse(Path.of("hosts/test/content/test.md"));
		
		Assertions.assertThat(content.meta()).containsKeys("title", "tags", "template");
		Assertions.assertThat(content.meta().get("title")).isEqualTo("Startseite");
		Assertions.assertThat(content.meta().get("tags")).isInstanceOf(List.class)
				.asList().containsExactly("eins", "zwei", "drei");
		Assertions.assertThat(content.content()).isEqualToIgnoringWhitespace(expectedMD);
	}
	
	@Test
	public void test_date() throws IOException {
		var contentParser = new ContentParser(new FileSystem(Path.of("hosts/test/"), new EventBus()));
		
		var content = contentParser.parse(Path.of("hosts/test/content/test.md"));
		
		System.out.println(content.meta().get("date"));
		System.out.println(content.meta().get("date").getClass());
		System.out.println(content.meta().get("datetime"));
		System.out.println(content.meta().get("datetime").getClass());
	}
}
