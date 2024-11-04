package com.condation.cms.content.shortcodes;

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

import org.apache.commons.jexl3.JexlBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author t.marx
 */
public class TagParserTest {
	
	TagParser tagParser;

	TagMap tagMap;
	
	@BeforeEach
	void setup() {
		tagMap = new TagMap();
		tagMap.put("code", params -> {
			// Verarbeitung der Parameter hier
			return "Ausgabe des Shortcodes";
		});
		tagMap.put("content", params -> {
			return (String)params.get("_content");
		});
		
		tagMap.put("exp", params -> {
			return "expression: " + params.get("value");
		});
		
		tagMap.put("param", params -> {
			return "param: " + params.get("param1");
		});
		
		tagMap.put("ns1:print", params -> {
			return "message: " + params.get("message");
		});
		
		this.tagParser = new TagParser(new JexlBuilder().create());
	}

	@Test
	public void no_shortcode() {
		String result = tagParser.parse("Dein Shortcode-Text hier", tagMap);
		Assertions.assertThat(result).isEqualTo("Dein Shortcode-Text hier");
	}
	
	@Test
	public void self_closing_tag() {
		String result = tagParser.parse("[[code/]]", tagMap);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void self_closing_tag_with_space() {
		String result = tagParser.parse("[[code /]]", tagMap);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}

	@Test
	public void end_closing_tag() {
		String result = tagParser.parse("[[code]][[/code]]", tagMap);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void tag_with_content() {
		String result = tagParser.parse("[[content]]Hello CondationCMS[[/content]]", tagMap);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}
	
	@Test
	public void expressions() {
		String result = tagParser.parse("[[exp value=\"${5+4}\"/]]", tagMap);
		Assertions.assertThat(result).isEqualTo("expression: 9");
	}
	
	@Test
	public void parameters_string() {
		String result = tagParser.parse("[[param param1=\"5\"/]]", tagMap);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_number() {
		String result = tagParser.parse("[[param param1=5 /]]", tagMap);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_boolean_true() {
		String result = tagParser.parse("[[param param1=true /]]", tagMap);
		Assertions.assertThat(result).isEqualTo("param: true");
	}
	
	@Test
	public void parameters_boolean_false() {
		String result = tagParser.parse("[[param param1=false /]]", tagMap);
		Assertions.assertThat(result).isEqualTo("param: false");
	}
	
	@Test
	public void parameters_with_content() {
		String result = tagParser.parse("[[param param1=\"5\"]]Hello[[/param]]", tagMap);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void shortCode_in_text() {
		String result = tagParser.parse("Hello [[content]]CondationCMS[[/content]]!", tagMap);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS!");
	}
	
	@Test
	public void namespace() {
		String result = tagParser.parse("[[ns1:print message='Hello CondationCMS']][[/ns1:print]]", tagMap);
		Assertions.assertThat(result).isEqualTo("message: Hello CondationCMS");
		
		result = tagParser.parse("[[ns1:print message='Hello CondationCMS' /]]", tagMap);
		Assertions.assertThat(result).isEqualTo("message: Hello CondationCMS");
	}
}