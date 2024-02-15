package com.github.thmarx.cms.markdown.module;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.slugify.Slugify;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.markdown.CMSMarkdown;
import com.github.thmarx.cms.markdown.Options;
import com.github.thmarx.cms.markdown.rules.block.CodeBlockRule;
import com.github.thmarx.cms.markdown.rules.block.HeadingBlockRule;
import com.github.thmarx.cms.markdown.rules.inline.ItalicInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.ImageInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.LinkInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.NewlineInlineRule;
import com.github.thmarx.cms.markdown.rules.inline.StrongInlineRule;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

/**
 *
 * @author t.marx
 */
@Slf4j
public class CMSMarkdownRenderer implements MarkdownRenderer {

	final Slugify slug = Slugify.builder().build();
	
	final CMSMarkdown renderer;
	
	public CMSMarkdownRenderer() {
		Options options = new Options();
		options.addInlineRule(new StrongInlineRule());
		options.addInlineRule(new ItalicInlineRule());
		options.addInlineRule(new NewlineInlineRule());
		options.addInlineRule(new ImageInlineRule());
		options.addInlineRule(new LinkInlineRule());
		options.addBlockRule(new CodeBlockRule());
		options.addBlockRule(new HeadingBlockRule());
		renderer = new CMSMarkdown(options);
	}

	@Override
	public void close() {
	}

	@Override
	public String excerpt(String markdown, int length) {
		String content = render(markdown);
		String text = Jsoup.parse(content).text();

		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}

	@Override
	public String render(String markdown) {
		try {
			return renderer.render(markdown);
		} catch (IOException ex) {
			log.error("", ex);
		}
		return markdown;
	}

}
