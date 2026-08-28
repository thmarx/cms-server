package com.condation.cms.api.configuration.configs;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.configuration.Config;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;

/**
 * Reloadable, site-scoped collection definitions.
 */
@RequiredArgsConstructor
public class CollectionConfiguration implements Config {

	private final ConcurrentMap<String, CollectionDefinition> collections;

	public Optional<CollectionDefinition> collection(String name) {
		return Optional.ofNullable(collections.get(name));
	}

	public Map<String, CollectionDefinition> collections() {
		return Map.copyOf(collections);
	}
}
