/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.utils;

import com.github.thmarx.cms.Constants;
import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class NodeUtilNGTest {

	public NodeUtilNGTest() {
	}

	@Test
	public void getName_returns_default_name() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of());

		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("index");
	}

	@Test
	public void getName_returns_title() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title"
		));

		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("The Title");
	}

	@Test
	public void getName_returns_title_if_emtpy_menu() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title",
				"menu", Map.of(
				)
		));
		
		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("The Title");
	}
	
	@Test
	public void getName_returns_menu_title() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title",
				"menu", Map.of(
						"title", "Menu title"
				)
		));
		
		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("Menu title");
	}
	
	@Test
	public void getMenuOrder() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"menu", Map.of(
						"order", 1.5
				)
		));
		var order = NodeUtil.getMenuOrder(node);
		Assertions.assertThat(order).isEqualTo(1.5f);
	}
	
	@Test
	public void getDefaultMenuOrder() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"menu", Map.of()
		));
		var order = NodeUtil.getMenuOrder(node);
		Assertions.assertThat(order).isEqualTo(Constants.DEFAULT_MENU_ORDER);
	}
	
	@Test
	public void getDefaultMenuOrderNoMenuMap() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of());
		var order = NodeUtil.getMenuOrder(node);
		Assertions.assertThat(order).isEqualTo(Constants.DEFAULT_MENU_ORDER);
	}
}
