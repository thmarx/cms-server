package com.condation.cms.templates.filter;

/*-
 * #%L
 * templates
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for type-safe FilterParams wrapper.
 */
public class FilterParamsTest {

	@Test
	public void testStringParameter() {
		FilterParams params = new FilterParams("hello", "world");

		Assertions.assertThat(params.size()).isEqualTo(2);
		Assertions.assertThat(params.getString(0)).isEqualTo("hello");
		Assertions.assertThat(params.getString(1)).isEqualTo("world");
	}

	@Test
	public void testStringParameterWithDefault() {
		FilterParams params = new FilterParams("hello");

		Assertions.assertThat(params.getString(0, "default")).isEqualTo("hello");
		Assertions.assertThat(params.getString(1, "default")).isEqualTo("default");
		Assertions.assertThat(params.getString(10, "fallback")).isEqualTo("fallback");
	}

	@Test
	public void testIntParameter() {
		FilterParams params = new FilterParams(42, "100");

		Assertions.assertThat(params.getInt(0)).isEqualTo(42);
		Assertions.assertThat(params.getInt(1)).isEqualTo(100); // String to int conversion
	}

	@Test
	public void testIntParameterWithDefault() {
		FilterParams params = new FilterParams(42);

		Assertions.assertThat(params.getInt(0, 0)).isEqualTo(42);
		Assertions.assertThat(params.getInt(1, 99)).isEqualTo(99);
		Assertions.assertThat(params.getInt(10, 123)).isEqualTo(123);
	}

	@Test
	public void testLongParameter() {
		FilterParams params = new FilterParams(1000L, "2000");

		Assertions.assertThat(params.getLong(0)).isEqualTo(1000L);
		Assertions.assertThat(params.getLong(1)).isEqualTo(2000L);
	}

	@Test
	public void testBooleanParameter() {
		FilterParams params = new FilterParams(true, "false", "yes");

		Assertions.assertThat(params.getBoolean(0)).isTrue();
		Assertions.assertThat(params.getBoolean(1)).isFalse();
		Assertions.assertThat(params.getBoolean(2)).isFalse(); // "yes" -> false
	}

	@Test
	public void testBooleanParameterWithDefault() {
		FilterParams params = new FilterParams(true);

		Assertions.assertThat(params.getBoolean(0, false)).isTrue();
		Assertions.assertThat(params.getBoolean(1, true)).isTrue();
		Assertions.assertThat(params.getBoolean(10, false)).isFalse();
	}

	@Test
	public void testTypedGet() {
		FilterParams params = new FilterParams("test", 123, true);

		Assertions.assertThat(params.get(0, String.class)).isEqualTo("test");
		Assertions.assertThat(params.get(1, Integer.class)).isEqualTo(123);
		Assertions.assertThat(params.get(2, Boolean.class)).isTrue();
	}

	@Test
	public void testTypedGetWithWrongType() {
		FilterParams params = new FilterParams("test");

		Assertions.assertThatThrownBy(() -> params.get(0, Integer.class))
				.isInstanceOf(ClassCastException.class)
				.hasMessageContaining("expected java.lang.Integer");
	}

	@Test
	public void testIndexOutOfBounds() {
		FilterParams params = new FilterParams("test");

		Assertions.assertThatThrownBy(() -> params.getString(10))
				.isInstanceOf(IndexOutOfBoundsException.class)
				.hasMessageContaining("index 10 out of bounds");
	}

	@Test
	public void testEmptyParams() {
		FilterParams params = new FilterParams();

		Assertions.assertThat(params.isEmpty()).isTrue();
		Assertions.assertThat(params.size()).isEqualTo(0);
	}

	@Test
	public void testNullParams() {
		FilterParams params = new FilterParams((Object[]) null);

		Assertions.assertThat(params.isEmpty()).isTrue();
		Assertions.assertThat(params.size()).isEqualTo(0);
	}

	@Test
	public void testHasParameter() {
		FilterParams params = new FilterParams("a", "b", "c");

		Assertions.assertThat(params.has(0)).isTrue();
		Assertions.assertThat(params.has(1)).isTrue();
		Assertions.assertThat(params.has(2)).isTrue();
		Assertions.assertThat(params.has(3)).isFalse();
		Assertions.assertThat(params.has(-1)).isFalse();
	}

	@Test
	public void testRealWorldUsage() {
		// Simulate a date filter with format parameter
		FilterParams params = new FilterParams("dd.MM.yyyy");

		String pattern = params.getString(0, "yyyy-MM-dd");
		Assertions.assertThat(pattern).isEqualTo("dd.MM.yyyy");

		// Simulate missing parameter
		FilterParams emptyParams = new FilterParams();
		String defaultPattern = emptyParams.getString(0, "yyyy-MM-dd");
		Assertions.assertThat(defaultPattern).isEqualTo("yyyy-MM-dd");
	}

	@Test
	public void testNumberConversion() {
		// Test various number types
		FilterParams params = new FilterParams(42, 42L, 42.5);

		Assertions.assertThat(params.getInt(0)).isEqualTo(42);
		Assertions.assertThat(params.getLong(1)).isEqualTo(42L);
		Assertions.assertThat(params.getInt(2)).isEqualTo(42); // Double -> int
	}
}
