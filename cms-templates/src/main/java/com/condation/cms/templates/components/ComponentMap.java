package com.condation.cms.templates.components;

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
import com.condation.cms.api.model.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ComponentMap {

	private final Map<String, Function<Parameter, String>> tags = new HashMap<>();

	public Set<String> names() {
		return Collections.unmodifiableSet(tags.keySet());
	}

	public void put(String codeName, Function<Parameter, String> function) {
		tags.put(codeName, function);
	}

	public void putAll(Map<String, Function<Parameter, String>> tags) {
		this.tags.putAll(tags);
	}

	public boolean has(String codeName) {
		return tags.containsKey(codeName);
	}

	public Function<Parameter, String> get(String name) {
		return tags.getOrDefault(name, (params) -> "");
	}
}
