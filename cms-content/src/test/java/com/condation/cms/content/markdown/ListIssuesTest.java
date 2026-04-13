package com.condation.cms.content.markdown;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ListIssuesTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		SUT = new CMSMarkdown(Options.all());
	}

		@Test
	void test_dot_issue_183() throws IOException {

		String md = "1. **first** sentence. second sentence.\n1. item 2";
		String expected = """
                    <ol><li><strong>first</strong> sentence. second sentence.</li><li>item 2</li></ol>
                    """;

		String html = SUT.render(md);

		
		Assertions.assertThat(html).isEqualToIgnoringWhitespace(expected);
	}
}
