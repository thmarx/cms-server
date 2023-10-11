/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.template.functions.list;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class NodeListFunctionBuilderNGTest {
	
	NodeListFunctionBuilder nodeList;
	private FileSystem fileSystem;
	
	@BeforeClass
	void setup () throws IOException {
		fileSystem = new FileSystem(Path.of("hosts/test"));
		fileSystem.init();
		ContentParser parser = new ContentParser(fileSystem);
		nodeList = new NodeListFunctionBuilder(fileSystem, fileSystem.resolve("content/").resolve("index.md"), parser);
	}
	@AfterClass
	void close () {
		fileSystem.shutdown();
	}

	@Test
	public void test_blog_entry() {
		Page<Node> page = nodeList.from("/blog/*").page(1).size(10).list();
		Assertions.assertThat(page.getItems()).hasSize(2);
	}
	
	@Test
	public void test_prodcuts_with_index() {
		Page<Node> page = nodeList.from("/products").page(1).size(10).index(true).list();
		Assertions.assertThat(page.getItems()).hasSize(2);
	}
	
	@Test
	public void test_prodcuts_without_index() {
		Page<Node> page = nodeList.from("/products").page(1).size(10).index(false).list();
		Assertions.assertThat(page.getItems()).hasSize(1);
	}
	
}
