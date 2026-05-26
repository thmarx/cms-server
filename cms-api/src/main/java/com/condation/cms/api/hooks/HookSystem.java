package com.condation.cms.api.hooks;

/*-
 * #%L
 * CMS Api
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
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Request based hook system.
 *
 * @author t.marx
 */
public interface HookSystem {

	void register(Object sourceObject);

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction);

	public <T> void registerAction(final String name, final ActionFunction<T> hookFunction, int priority);

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction);

	public <T> void registerFilter(final String name, final FilterFunction<T> hookFunction, int priority);

	public ActionContext<Object> doAction(final String name);

	public ActionContext<Object> doAction(final String name, final Map<String, Object> arguments);

	/**
	 * calls all filters with the given parameters, if no filter is executed,
	 * the original parameters are returned
	 *
	 * @param <T>
	 * @param name
	 * @param parameters
	 * @return
	 */
	public <T> FilterContext<T> doFilter(final String name, final T parameters);
}
