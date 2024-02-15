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
import com.github.thmarx.cms.markdown.rules.block.ParagraphBlockRule;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author t.marx
 */
public class CMSMarkdown {

	private final BlockTokenizer blockTokenizer;

	private final List<BlockElementRule> blockRules;
	private final List<InlineElementRule> inlineRules;

	public CMSMarkdown(Options options) {
		this.blockTokenizer = new BlockTokenizer(options);
		blockRules = options.blockElementRules;
		blockRules.addLast(new ParagraphBlockRule());
		inlineRules = options.inlineElementRules;
	}

	public String render(final String md) throws IOException {
		final StringBuilder htmlBuilder = new StringBuilder();
		List<Block> blocks = blockTokenizer.tokenize(escape(md));

		blocks.stream()
				.map(block -> {

					if (block instanceof BlockContainer) {
						final Function<String, String> renderContainer = (content) -> {
							try {
								return this.render(content);
							} catch (IOException e) {
							}
							return "";
						};
						return ((BlockContainer) block).render(renderContainer);
					} else {
						return block.render();
					}
				})
				.forEach(blockHtml -> {
					final StringBuilder html = new StringBuilder(blockHtml);
					inlineRules.forEach(rule -> html.replace(0, html.length(), rule.render(html.toString())));
					htmlBuilder.append(html);
				});

		return htmlBuilder.toString();
	}

	private String escape(final String md) {
		return md
				.replaceAll("\\\\#", "&#35;")
				.replaceAll("\\\\\\*", "&#42;")
				.replaceAll("\\\\`", "&#96;")
				.replaceAll("\\\\_", "&#95;")
				.replaceAll("\\\\\\{", "&#123;")
				.replaceAll("\\\\\\}", "&#125;")
				.replaceAll("\\\\\\[", "&#91;")
				.replaceAll("\\\\\\]", "&#93;")
				.replaceAll("\\\\<", "&#60;")
				.replaceAll("\\\\>", "&#62;")
				.replaceAll("\\\\\\(", "&#40;")
				.replaceAll("\\\\\\)", "&#41;")
				.replaceAll("\\\\\\+", "&#43;")
				.replaceAll("\\\\-", "&#45;")
				.replaceAll("\\\\\\.", "&#46;")
				.replaceAll("\\\\!", "&#33;")
				.replaceAll("\\\\\\|", "&#124;");
	}
}
