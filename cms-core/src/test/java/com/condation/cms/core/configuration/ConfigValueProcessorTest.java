package com.condation.cms.core.configuration;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class ConfigValueProcessorTest {

    @BeforeEach
    void setUp() {
        EnvironmentVariables.resetForTesting();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("test.var");
        System.clearProperty("list.var");
        System.clearProperty("map.var");
        System.clearProperty("nested.var");
    }

    @Test
    void testProcessStringWithKnownVariable() {
        System.setProperty("test.var", "test_value");
        String input = "This is a ${env:test.var}";
        String expected = "This is a test_value";
        assertEquals(expected, ConfigValueProcessor.process(input));
    }

    @Test
    void testProcessStringWithUnknownVariable() {
        String input = "This is a ${env:unknown.var}";
        assertEquals(input, ConfigValueProcessor.process(input));
    }

    @Test
    void testProcessList() {
        System.setProperty("list.var", "item2");
        List<String> input = List.of("item1", "${env:list.var}", "item3");
        List<String> expected = List.of("item1", "item2", "item3");
        assertEquals(expected, ConfigValueProcessor.process(input));
    }

    @Test
    void testProcessMap() {
        System.setProperty("map.var", "value2");
        Map<String, String> input = Map.of("key1", "value1", "key2", "${env:map.var}");
        Map<String, String> expected = Map.of("key1", "value1", "key2", "value2");
        assertEquals(expected, ConfigValueProcessor.process(input));
    }

    @Test
    void testProcessNestedMap() {
        System.setProperty("nested.var", "nested_value");
        Map<String, Object> input = Map.of("key1", "value1", "nested", Map.of("key2", "${env:nested.var}"));
        Map<String, Object> expected = Map.of("key1", "value1", "nested", Map.of("key2", "nested_value"));
        assertEquals(expected, ConfigValueProcessor.process(input));
    }

    @Test
    void testProcessNullValue() {
        assertNull(ConfigValueProcessor.process(null));
    }

    @Test
    void testProcessNonStringValue() {
        Integer input = 123;
        assertEquals(input, ConfigValueProcessor.process(input));
    }
}
