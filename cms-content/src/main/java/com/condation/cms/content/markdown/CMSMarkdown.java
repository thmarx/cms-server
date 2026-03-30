package com.condation.cms.content.markdown;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;
import com.condation.cms.content.markdown.rules.inline.TextInlineRule;
import com.condation.cms.content.markdown.utils.StringUtils;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class CMSMarkdown {

	private final BlockTokenizer blockTokenizer;

	private final InlineElementTokenizer inlineTokenizer;

	private final List<BlockElementRule> blockRules;
	private final List<InlineElementRule> inlineRules;

	public CMSMarkdown(Options options) {
		this.blockTokenizer = new BlockTokenizer(options);
		this.inlineTokenizer = new InlineElementTokenizer(options);
		blockRules = options.blockElementRules;
		blockRules.addLast(new ParagraphBlockRule());
		inlineRules = options.inlineElementRules;
		inlineRules.addLast(new TextInlineRule());
	}

	private String renderInlineElements(final String inline_md) throws IOException {
		List<InlineBlock> blocks = inlineTokenizer.tokenize(inline_md);

		// Pre-size StringBuilder based on input length to reduce allocations
		final StringBuilder htmlBuilder = new StringBuilder(inline_md.length() + 128);

		// Use simple loop instead of streams for better performance
		for (InlineBlock block : blocks) {
			htmlBuilder.append(block.render());
		}

		return htmlBuilder.toString();
	}

	public String render(final String md) throws IOException {
		// Escape input markdown
		String escapedMd = StringUtils.escape(md);
		List<Block> blocks = blockTokenizer.tokenize(escapedMd);

		// Pre-size StringBuilder based on input to reduce allocations
		final StringBuilder htmlBuilder = new StringBuilder(md.length() + 256);

		// Create renderers once instead of as lambdas
		InlineRenderer inlineRenderer = (content) -> {
			try {
				return renderInlineElements(content);
			} catch (IOException ioe) {
				// Log error but don't break rendering
				return "";
			}
		};
		BlockRenderer blockRenderer = (content) -> {
			try {
				return this.render(content);
			} catch (IOException e) {
				// Log error but don't break rendering
				return "";
			}
		};

		// Use simple loop instead of streams for better performance
		for (Block block : blocks) {
			String rendered;
			if (block instanceof BlockContainer) {
				rendered = ((BlockContainer) block).render(blockRenderer);
			} else {
				rendered = block.render(inlineRenderer);
			}
			htmlBuilder.append(rendered);
		}

		return StringUtils.unescape(htmlBuilder.toString());
	}
}
