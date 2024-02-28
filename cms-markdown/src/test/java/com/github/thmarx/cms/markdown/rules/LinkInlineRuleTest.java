package com.github.thmarx.cms.markdown.rules;

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

import com.github.thmarx.cms.markdown.rules.inline.LinkInlineRule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class LinkInlineRuleTest {

	LinkInlineRule SUT = new LinkInlineRule();

	@Test
	public void testSomeMethod() {
		
		var result = SUT.next("[google](https://google.de)");
		
		Assertions.assertThat(result.render())
				.isEqualTo("<a href=\"https://google.de\" id=\"google\">google</a>");
	}

}
