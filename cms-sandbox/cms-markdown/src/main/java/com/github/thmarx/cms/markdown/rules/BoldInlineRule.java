package com.github.thmarx.cms.markdown.rules;

import com.github.thmarx.cms.markdown.InlineElementRule;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class BoldInlineRule implements InlineElementRule {
	
	Pattern bold = Pattern.compile("\\*{2}(.*?)\\*{2}");

	@Override
	public String render(String md) {
		var matcher = bold.matcher(md);
		return matcher.replaceAll((result) -> "<strong>%s</strong>".formatted(result.group(1)));
	}
	
	
}
