package com.github.thmarx.cms.api.hooks;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 Marx-Software
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class HookSystemTest {

	private HookSystem hookSystem;
	
	@BeforeEach
	public void setup() {
		hookSystem = new HookSystem(null);
	}

	@Test
	public void test_single_result() {
		hookSystem.register("test/test1", (context) -> {
			return true;
		});
		HookContext context = hookSystem.call("test/test1");
		Assertions.assertThat(context.results()).hasSize(1).contains(true);
	}
	
	@Test
	public void test_multiple_result() {
		hookSystem.register("test/test1", (context) -> {
			return "test1";
		});
		hookSystem.register("test/test1", (context) -> {
			return "test2";
		});
		HookContext context = hookSystem.call("test/test1");
		Assertions.assertThat(context.results()).hasSize(2).contains("test1", "test2");
	}
	
	@Test
	public void test_multiple_result_with_priority() {
		hookSystem.register("test/test1", (context) -> {
			return "test3";
		}, 300);
		hookSystem.register("test/test1", (context) -> {
			return "test1";
		}, 100);
		hookSystem.register("test/test1", (context) -> {
			return "test2";
		}, 200);
		HookContext context = hookSystem.call("test/test1");
		Assertions.assertThat(context.results()).hasSize(3).containsExactly("test1", "test2", "test3");
	}
	
	@Test
	public void test_multiple_result_with_priority_reversed() {
		hookSystem.register("test/test1", (context) -> {
			return "test3";
		}, 100);
		hookSystem.register("test/test1", (context) -> {
			return "test1";
		}, 300);
		hookSystem.register("test/test1", (context) -> {
			return "test2";
		}, 200);
		HookContext context = hookSystem.call("test/test1");
		Assertions.assertThat(context.results()).hasSize(3).containsExactly("test3", "test2", "test1");
	}
}
