package com.github.thmarx.cms.markdown.rules;

import com.github.thmarx.cms.markdown.InlineElementRule;
import java.util.regex.Pattern;

/**
 *
 * @author t.marx
 */
public class NewlineInlineRule implements InlineElementRule {
	
	Pattern bold = Pattern.compile("[ ]{2,}\\n");

	@Override
	public String render(String md) {
		var matcher = bold.matcher(md);
		return matcher.replaceAll("<br/>");
	}
	
	
}
