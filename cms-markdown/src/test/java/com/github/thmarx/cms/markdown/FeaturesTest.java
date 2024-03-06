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
import com.github.thmarx.cms.markdown.rules.block.BlockquoteBlockRule;
import com.github.thmarx.cms.markdown.rules.block.CodeBlockRule;
import com.github.thmarx.cms.markdown.rules.block.DefinitionListBlockRule;
import com.github.thmarx.cms.markdown.rules.block.HeadingBlockRule;
import com.github.thmarx.cms.markdown.rules.block.HorizontalRuleBlockRule;
import com.github.thmarx.cms.markdown.rules.block.ListBlockRule;
import com.github.thmarx.cms.markdown.rules.block.TableBlockRule;
import com.github.thmarx.cms.markdown.rules.block.TaskListBlockRule;
import com.github.thmarx.cms.markdown.rules.inline.HighlightInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.ItalicInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.ImageInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.LinkInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.StrongInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.NewlineInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.StrikethroughInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.SubscriptInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.SuperscriptInlineRule;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

/**
 *
 * @author t.marx
 */
public class FeaturesTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		Options options = new Options();
		options.addInlineRule(new StrongInlineRule());
		options.addInlineRule(new ItalicInlineRule());
		options.addInlineRule(new NewlineInlineRule());
		options.addInlineRule(new ImageInlineRule());
		options.addInlineRule(new LinkInlineRule());
		options.addInlineRule(new StrikethroughInlineRule());
		options.addInlineRule(new HighlightInlineRule());
		options.addInlineRule(new SubscriptInlineRule());
		options.addInlineRule(new SuperscriptInlineRule());

		options.addBlockRule(new CodeBlockRule());
		options.addBlockRule(new HeadingBlockRule());
		options.addBlockRule(new TaskListBlockRule());
		options.addBlockRule(new ListBlockRule());
		options.addBlockRule(new HorizontalRuleBlockRule());
		options.addBlockRule(new BlockquoteBlockRule());
		options.addBlockRule(new TableBlockRule());
		options.addBlockRule(new DefinitionListBlockRule());
		SUT = new CMSMarkdown(options);
	}

	@RepeatedTest(1)
	public void test_features() throws IOException {

		var md = load("features.md").trim();
		var expected = load("features.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_tables() throws IOException {

		var md = load("features.tables.md").trim();
		var expected = load("features.tables.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_definition_lists() throws IOException {

		var md = load("features.dl.md").trim();
		var expected = load("features.dl.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_tasklist() throws IOException {

		var md = load("features.tasklist.md").trim();
		var expected = load("features.tasklist.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
}
