package com.github.thmarx.cms;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.regex.Matcher;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class ConstantsNGTest {
	
	

	@Test
	public void test_secion_pattern() {
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher("index.md").matches()).isFalse();
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher(".section.md").matches()).isFalse();
		
		Matcher matcher = Constants.SECTION_PATTERN.matcher("page.section.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("section");
		Assertions.assertThat(matcher.group("section")).isEqualTo("section");
		
		matcher = Constants.SECTION_PATTERN.matcher("index.card.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("card");
		Assertions.assertThat(matcher.group("section")).isEqualTo("card");
	}
	
}
