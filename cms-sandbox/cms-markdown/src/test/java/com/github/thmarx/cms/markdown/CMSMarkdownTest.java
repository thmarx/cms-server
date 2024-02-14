package com.github.thmarx.cms.markdown;

/*-
 * #%L
 * cms-markdown
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.thmarx.cms.markdown.rules.block.CodeBlockRule;
import com.github.thmarx.cms.markdown.rules.block.HeadingBlockRule;
import com.github.thmarx.cms.markdown.rules.block.HorizontalRuleBlockRule;
import com.github.thmarx.cms.markdown.rules.block.ListBlockRule;
import com.github.thmarx.cms.markdown.rules.inline.EmphasisInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.ImageInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.LinkInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.StrongInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.NewlineInlineRule;
import java.io.IOException;
import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class CMSMarkdownTest extends MarkdownTest {
	
	static CMSMarkdown SUT;
	
	@BeforeAll
	public static void setup () {
		Options options = new Options();
		options.addInlineRule(new StrongInlineRule());
		options.addInlineRule(new EmphasisInlineRule());
		options.addInlineRule(new NewlineInlineRule());
		options.addInlineRule(new LinkInlineRule());
		options.addInlineRule(new ImageInlineRule());
		
		options.addBlockRule(new CodeBlockRule());
		options.addBlockRule(new HeadingBlockRule());
		options.addBlockRule(new ListBlockRule());
		options.addBlockRule(new HorizontalRuleBlockRule());
		
		SUT = new CMSMarkdown(options);
	}
	
	@Test
	public void test_1() throws IOException {
		
		var md = load("render/test_1.md").trim();
		var expected = load("render/test_1.html");
		expected = removeComments(expected);
		
		var result = SUT.render(md);
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	public void test_2() throws IOException {
		
		var md = load("render/test_2.md");
		var expected = load("render/test_2.html");
		expected = removeComments(expected);
		
		var result = SUT.render(md);
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	public void test_3() throws IOException {
		
		var md = load("render/test_3.md");
		var expected = load("render/test_3.html");
		expected = removeComments(expected);
		
		var result = SUT.render(md);
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	public void test_4() throws IOException {
		
		var md = load("render/test_4.md");
		var expected = load("render/test_4.html");
		expected = removeComments(expected);
		
		var result = SUT.render(md);
		System.out.println(result);
//		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@ParameterizedTest
	@CsvSource({
		"\\!test\\!,<p>&#33;test&#33;</p>",
		"\\|test\\|,<p>&#124;test&#124;</p>",
		"\\.test\\.,<p>&#46;test&#46;</p>",
		"\\-test\\-,<p>&#45;test&#45;</p>",
		"\\+test\\+,<p>&#43;test&#43;</p>",
		"\\(test\\),<p>&#40;test&#41;</p>",
		"\\<test\\>,<p>&#60;test&#62;</p>",
		"\\[test\\],<p>&#91;test&#93;</p>",
		"\\{test\\},<p>&#123;test&#125;</p>",
		"\\#test\\#,<p>&#35;test&#35;</p>",
		"\\_test\\_,<p>&#95;test&#95;</p>",
		"\\`test\\`,<p>&#96;test&#96;</p>",
		"\\*test\\*,<p>&#42;test&#42;</p>"
	})
	void test_escape (final String input, final String expected) throws IOException {
		var result = SUT.render(input);
		Assertions.assertThat(result).isEqualTo(expected);
	}
	
	@Test
	void test_horizontal_rule_with_before() throws IOException {
		
		String input = """
                 before
                 
                 ---
                 
                 after
                 """;
		
		
		var result = SUT.render(input);

		Assertions.assertThat(result).isEqualTo("<p>before</p><hr /><p>after</p><p></p>");
	}
}
