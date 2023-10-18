/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.markdown.FlexMarkMarkdownRenderer;
import com.github.thmarx.cms.markdown.MarkdownRenderer;
import com.github.thmarx.cms.template.TemplateEngine;
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
		markdownRenderer = new FlexMarkMarkdownRenderer();
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
