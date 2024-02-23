package com.github.thmarx.cms.markdown.rules.block;

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

import com.github.thmarx.cms.markdown.Block;
import com.github.thmarx.cms.markdown.BlockElementRule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class CodeBlockRule implements BlockElementRule {

	protected static final Pattern PATTERN = Pattern.compile(
			"^(`{3})(?<lang>[a-zA-Z0-9]*)?$\n(?<code>[\\s\\S]*?)\n^(`{3})$",
			Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES);
	
	@Override
	public Block next(final String md) {
		Matcher matcher = PATTERN.matcher(md);
		if (matcher.find()) {
			return new CodeBlock(matcher.start(), matcher.end(), matcher.group("code"), matcher.group("lang"));
		}
		return null;
	}

	
	public static record CodeBlock (int start, int end, String content, String language) implements Block {

		@Override
		public String render() {
			if (language == null || "".equals(language)) {
				return "<pre><code>%s</code></pre>".formatted(content);
			}
			return "<pre><code class='lang-%s'>%s</code></pre>".formatted(language, content);
		}
		
	}
	
}
