package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class YamlProperties {
	
	protected final Map<String, Object> properties;

	protected YamlProperties (final Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public Object get(final String name) {
		return properties.get(name);
	}

	public <T> T getOrDefault(final String name, final T defaultValue) {
		return (T) properties.getOrDefault(name, defaultValue);
	}

	protected Map<String, Object> getSubMap(final String name) {
		return (Map<String, Object>) properties.getOrDefault(name, Collections.emptyMap());
	}
	
}
