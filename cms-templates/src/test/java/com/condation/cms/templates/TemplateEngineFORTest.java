package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateEngineFORTest extends AbstractTemplateEngineTest {

    @Override
    public TemplateLoader getLoader() {
        return new StringTemplateLoader()
                // LIST ITERATION
                .add("simple", """
                   {% for name in names %}
                       <li>{{ name }}</li>
                   {% endfor %}
                   """)
                // LOOP INDEX
                .add("index", """
                   {% for name in names %}
                       <li>{{ loop.getIndex() }}</li>
                   {% /for %}
                   """)
                // MAP ITERATION (key/value)
                .add("map", """
                   {% for key, value in data %}
                       <li>{{ key }}:{{ value }}</li>
                   {% endfor %}
                   """)
                // MIXED STRUCTURE: MapEntry loop object
                .add("map_entry_loop", """
                   {% for entry in data %}
                       <li>{{ entry.key }}={{ entry.value }}</li>
                   {% endfor %}
                   """)
                // EMPTY LIST
                .add("empty", """
                   {% for item in list %}
                       <li>{{ item }}</li>
                   {% endfor %}
                   """)
                // SINGLE ITEM
                .add("single", """
                   {% for item in list %}
                       <li>{{ item }}</li>
                   {% endfor %}
                   """);
    }

    @Test
    public void test_for_list() throws IOException {

        var expected = """
                 <li>one</li>
                 <li>two</li>
                 <li>three</li>
                 """;

        Template template = SUT.getTemplate("simple");

        Map<String, Object> context = Map.of(
                "names", List.of("one", "two", "three")
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void test_loop_index() throws IOException {

        var expected = """
                 <li>0</li>
                 <li>1</li>
                 <li>2</li>
                 """;

        Template template = SUT.getTemplate("index");

        Map<String, Object> context = Map.of(
                "names", List.of("a", "b", "c")
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void test_map_iteration_key_value() throws IOException {

        var expected = """
                 <li>a:1</li>
                 <li>b:2</li>
                 """;

        Template template = SUT.getTemplate("map");

        var data = new LinkedHashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        
        Map<String, Object> context = Map.of(
                "data", data
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void test_map_entry_loop_object() throws IOException {

        var expected = """
                 <li>a=1</li>
                 <li>b=2</li>
                 """;

        Template template = SUT.getTemplate("map_entry_loop");

        var data = new LinkedHashMap<>();
        data.put("a", 1);
        data.put("b", 2);
        
        Map<String, Object> context = Map.of(
                "data", data
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void test_empty_list() throws IOException {

        var expected = "";

        Template template = SUT.getTemplate("empty");

        Map<String, Object> context = Map.of(
                "list", List.of()
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void test_single_item() throws IOException {

        var expected = "<li>only</li>";

        Template template = SUT.getTemplate("single");

        Map<String, Object> context = Map.of(
                "list", List.of("only")
        );

        Assertions.assertThat(template.evaluate(context))
                .isEqualToIgnoringWhitespace(expected);
    }
}
