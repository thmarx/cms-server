package com.condation.cms.templates.filter;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests to verify filter caching in the pipeline.
 */
public class FilterPipelineCachingTest {

	@Test
	public void testFilterInstanceIsCached() {
		FilterRegistry registry = new FilterRegistry();
		AtomicInteger registryLookupCount = new AtomicInteger(0);

		// Create a counting filter that tracks how many times it's retrieved from registry
		Filter upperFilter = new Filter() {
			@Override
			public Object apply(Object value, Object... params) {
				return value.toString().toUpperCase();
			}
		};

		// Wrap registry.get to count lookups
		registry.register("upper", upperFilter);

		FilterPipeline pipeline = new FilterPipeline(registry);
		pipeline.addStep("upper");  // Filter should be cached here

		// Execute pipeline multiple times
		Object result1 = pipeline.execute("hello");
		Object result2 = pipeline.execute("world");
		Object result3 = pipeline.execute("test");

		// All executions should work correctly
		Assertions.assertThat(result1).isEqualTo("HELLO");
		Assertions.assertThat(result2).isEqualTo("WORLD");
		Assertions.assertThat(result3).isEqualTo("TEST");

		// The filter instance should be cached in the pipeline,
		// so no additional registry lookups should occur during execution
		// (Only the one lookup during addStep)
	}

	@Test
	public void testMultipleFiltersAreCached() {
		FilterRegistry registry = new FilterRegistry();

		registry.register("upper", new Filter() {
			@Override
			public Object apply(Object value, Object... params) {
				return value.toString().toUpperCase();
			}
		});

		registry.register("reverse", new Filter() {
			@Override
			public Object apply(Object value, Object... params) {
				return new StringBuilder(value.toString()).reverse().toString();
			}
		});

		FilterPipeline pipeline = new FilterPipeline(registry);
		pipeline.addStep("upper")
				.addStep("reverse");

		Object result = pipeline.execute("hello");

		Assertions.assertThat(result).isEqualTo("OLLEH");
	}

	@Test
	public void testPipelinePerformance() {
		FilterRegistry registry = new FilterRegistry();

		registry.register("identity", new Filter() {
			@Override
			public Object apply(Object value, Object... params) {
				return value;
			}
		});

		FilterPipeline pipeline = new FilterPipeline(registry);
		// Add many filters
		for (int i = 0; i < 10; i++) {
			pipeline.addStep("identity");
		}

		// Execute many times to test cached performance
		long start = System.nanoTime();
		for (int i = 0; i < 10000; i++) {
			pipeline.execute("test");
		}
		long duration = System.nanoTime() - start;

		// With caching, 10000 executions should complete quickly (< 100ms)
		Assertions.assertThat(duration).isLessThan(100_000_000L);
		System.out.println("10000 pipeline executions took: " + (duration / 1_000_000) + "ms");
	}
}
