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

import java.util.ArrayList;
import java.util.List;

/**
 * A pipeline for applying multiple filters in sequence.
 * Filter instances are cached at pipeline construction time for optimal performance.
 */
public class FilterPipeline {
	private final List<PipelineStep> steps = new ArrayList<>();
	private final FilterRegistry registry;

	public FilterPipeline(FilterRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Adds a filter step to the pipeline.
	 * The filter instance is cached immediately for better performance during execution.
	 *
	 * @param filterName the name of the filter
	 * @param params     the parameters to pass to the filter
	 * @return this pipeline for fluent chaining
	 */
	public FilterPipeline addStep(String filterName, Object... params) {
		if (!registry.exists(filterName)) {
			throw new IllegalArgumentException("Filter not found: " + filterName);
		}

		// Cache the filter instance at construction time
		Filter filter = registry.get(filterName);
		steps.add(new PipelineStep(filter, params));

		return this;
	}

	/**
	 * Executes all filters in the pipeline sequentially.
	 * Filter instances are already cached, so no registry lookups occur during execution.
	 *
	 * @param input the input value
	 * @return the result after applying all filters
	 */
	public Object execute(Object input) {
		Object result = input;
		for (PipelineStep step : steps) {
			result = step.filter.apply(result, step.params);
		}
		return result;
	}

	/**
	 * Represents a single step in the filter pipeline.
	 * Caches the filter instance to avoid registry lookups during execution.
	 */
	private static class PipelineStep {
		private final Filter filter;
		private final Object[] params;

		public PipelineStep(Filter filter, Object... params) {
			this.filter = filter;
			this.params = params;
		}
	}
}
