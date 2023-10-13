/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.utils;

import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class NameUtilNGTest {

	public NameUtilNGTest() {
	}

	@Test
	public void getName_returns_default_name() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of());

		var name = NameUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("index");
	}

	@Test
	public void getName_returns_title() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title"
		));

		var name = NameUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("The Title");
	}

	@Test
	public void getName_returns_title_if_emtpy_menu() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title",
				"menu", Map.of(
				)
		));
		
		var name = NameUtil.getName(node);

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
		
		var name = NameUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("Menu title");
	}
}
