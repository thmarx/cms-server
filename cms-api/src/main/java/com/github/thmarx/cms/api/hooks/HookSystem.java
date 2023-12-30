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
import com.github.thmarx.modules.api.ModuleManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * Request based hook system.
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class HookSystem {

	Multimap<String, Hook> hooks = ArrayListMultimap.create();

	public void register(final String name, final Function<HookContext, Object> hookFunction) {
		register(name, hookFunction, 10);
	}

	public void register(final String name, final Function<HookContext, Object> hookFunction, int priority) {
		hooks.put(name, new Hook(name, priority, hookFunction));
	}

	public HookContext call(final String name) {
		return call(name, Map.of());
	}
	
	public HookContext call(final String name, final Map<String, Object> arguments) {
		var context = new HookContext(new HashMap<String, Object>(arguments), new ArrayList<Object>());
		hooks.get(name).stream()
				.sorted((h1, h2) -> Integer.compare(h1.priority(), h2.priority()))
				.map(action -> action.function().apply(context))
				.forEach(context.results()::add);

		return context;
	}
}
