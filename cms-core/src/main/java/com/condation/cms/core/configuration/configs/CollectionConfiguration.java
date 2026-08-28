package com.condation.cms.core.configuration.configs;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.configuration.reload.NoReload;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads collection definitions from site configuration sources.
 */
@Slf4j
public class CollectionConfiguration extends AbstractConfiguration implements IConfiguration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;
	private final ConcurrentMap<String, CollectionDefinition> collections = new ConcurrentHashMap<>();

	private CollectionConfiguration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		reloadStrategy.register(this);
		reload();
	}

	public static Builder builder(EventBus eventBus) {
		return new Builder(eventBus);
	}

	@Override
	protected List<ConfigSource> getSources() {
		return sources;
	}

	@Override
	public String id() {
		return id;
	}

	public ConcurrentMap<String, CollectionDefinition> getCollections() {
		return collections;
	}

	@Override
	public void reload() {
		var reloaded = false;
		var updatedCollections = new ConcurrentHashMap<String, CollectionDefinition>();
		for (var source : sources) {
			reloaded |= source.reload();
			if (!source.exists()) {
				continue;
			}
			for (var entry : source.getMap("collections").entrySet()) {
				parse(entry.getKey(), entry.getValue()).ifPresent(definition ->
						updatedCollections.put(definition.name(), definition));
			}
		}

		collections.clear();
		collections.putAll(updatedCollections);
		if (reloaded && eventBus != null) {
			eventBus.publish(new ConfigurationReloadEvent(id));
		}
	}

	private java.util.Optional<CollectionDefinition> parse(String name, Object value) {
		try {
			if (!(value instanceof Map<?, ?> collection)) {
				throw new IllegalArgumentException("collection definition must be a map");
			}

			var detailValue = collection.get("detail");
			if (detailValue == null) {
				return java.util.Optional.of(new CollectionDefinition(name, null));
			}
			if (!(detailValue instanceof Map<?, ?> detail)) {
				throw new IllegalArgumentException("collection detail definition must be a map");
			}

			var route = stringValue(detail.get("route"), "route");
			var template = stringValue(detail.get("template"), "template");
			return java.util.Optional.of(new CollectionDefinition(
					name,
					new CollectionDetailConfiguration(route, template)));
		} catch (RuntimeException ex) {
			log.error("invalid configuration for collection {}", name, ex);
			return java.util.Optional.empty();
		}
	}

	private static String stringValue(Object value, String field) {
		if (!(value instanceof String string) || string.isBlank()) {
			throw new IllegalArgumentException("collection detail " + field + " must be a non-empty string");
		}
		return string;
	}

	public static class Builder {

		private final List<ConfigSource> sources = new ArrayList<>();
		private ReloadStrategy reloadStrategy = new NoReload();
		private String id = UUID.randomUUID().toString();
		private final EventBus eventBus;

		private Builder(EventBus eventBus) {
			this.eventBus = eventBus;
		}

		public Builder id(String uniqueId) {
			this.id = uniqueId;
			return this;
		}

		public Builder addSource(ConfigSource source) {
			sources.add(source);
			return this;
		}

		public Builder reloadStrategy(ReloadStrategy reload) {
			this.reloadStrategy = reload;
			return this;
		}

		public CollectionConfiguration build() {
			return new CollectionConfiguration(this);
		}
	}
}
