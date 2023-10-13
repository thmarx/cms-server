/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.template.functions.list;

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.template.functions.navigation.NavNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;
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
		fileSystem = new FileSystem(Path.of("hosts/test"), new EventBus());
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
	public void test_blog_entry_sorted() {
		Page<Node> page = nodeList.from("/blog/*")
				.page(1).size(10)
				.sort("published")
				.list();
		Assertions.assertThat(page.getItems()).hasSize(2);
		Assertions.assertThat(page.getItems().get(0).getName()).isEqualTo("September");
		Assertions.assertThat(page.getItems().get(1).getName()).isEqualTo("Oktober");
	}
	
	@Test
	public void test_blog_entry_sorted_reverse() {
		Page<Node> page = nodeList.from("/blog/*")
				.page(1).size(10)
				.sort("published")
				.reverse(true)
				.list();
		Assertions.assertThat(page.getItems()).hasSize(2);
		Assertions.assertThat(page.getItems().get(0).getName()).isEqualTo("Oktober");
		Assertions.assertThat(page.getItems().get(1).getName()).isEqualTo("September");
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
	
	
	@Test
	void list_root () {
		Page<Node> page = nodeList.from("/nodelist").page(1).size(10).list();
		var nodeUris = page.getItems().stream().map(Node::getPath).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder(
						"/nodelist", 
						"/nodelist/folder1", 
						"/nodelist/folder2"
				);
	}
	
	@Test
	void list_folder1 () {
		Page<Node> page = nodeList.from("/nodelist/folder1").page(1).size(10).list();
		var nodeUris = page.getItems().stream().map(Node::getPath).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder(
						"/nodelist/folder1", 
						"/nodelist/folder1/test"
				);
	}
	
	@Test
	void list_asterix () {
		Page<Node> page = nodeList.from("/nodelist/*").page(1).size(10).list();
		var nodeUris = page.getItems().stream().map(Node::getPath).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder(
						"/nodelist/folder1", 
						"/nodelist/folder1/test",
						"/nodelist/folder2",
						"/nodelist/folder2/test"
				);
	}
}
