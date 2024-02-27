package com.github.thmarx.cms.markdown.rules.inline;

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
import com.github.slugify.Slugify;
import com.github.thmarx.cms.api.feature.features.IsPreviewFeature;
import com.github.thmarx.cms.api.feature.features.SitePropertiesFeature;
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import com.github.thmarx.cms.markdown.InlineBlock;
import com.github.thmarx.cms.markdown.InlineElementRule;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class LinkInlineRule implements InlineElementRule {

	static final Slugify SLUG = Slugify.builder().build();

	static final Pattern PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

	
	
	@Override
	public InlineBlock next(String md) {
		var matcher = PATTERN.matcher(md);
		
		if (matcher.find()) {
			var href = matcher.group(2);
			var title = matcher.group(1);

			var id = SLUG.slugify(title);

			var requestContext = ThreadLocalRequestContext.REQUEST_CONTEXT.get();

			if (requestContext != null
					&& isInternalUrl(href)) {

				if (requestContext.has(SitePropertiesFeature.class)) {
					var contextPath = requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
					if (!"/".equals(contextPath) && !href.startsWith(contextPath) && href.startsWith("/")) {
						href = contextPath + href;
					}
				}
				if (requestContext.has(IsPreviewFeature.class)) {
					if (href.contains("?")) {
						href += "&preview";
					} else {
						href += "?preview";
					}
				}
			}

			return new LinkBlock(matcher.start(), matcher.end(), href, id, title);
		}
		
		return null;
	}
	
	private boolean isInternalUrl (final String href) {
		return !href.startsWith("http") && !href.startsWith("https");
	}

	public static record LinkBlock(int start, int end, String href, String id, String title) implements InlineBlock {
		@Override
		public String render() {
			return "<a href=\"%s\" id=\"%s\">%s</a>".formatted(
					href,
					id,
					title);
		}
	}
}
