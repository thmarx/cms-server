/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms;

import java.util.regex.Matcher;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class ConstantsNGTest {
	
	

	@Test
	public void test_secion_pattern() {
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher("index.md").matches()).isFalse();
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher(".section.md").matches()).isFalse();
		
		Matcher matcher = Constants.SECTION_PATTERN.matcher("page.section.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("section");
		Assertions.assertThat(matcher.group("section")).isEqualTo("section");
		
		matcher = Constants.SECTION_PATTERN.matcher("index.card.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("card");
		Assertions.assertThat(matcher.group("section")).isEqualTo("card");
	}
	
}
