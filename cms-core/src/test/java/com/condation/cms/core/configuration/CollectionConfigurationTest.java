package com.condation.cms.core.configuration;

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

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.core.configuration.configs.CollectionConfiguration;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CollectionConfigurationTest {

	@Test
	void updatesTheSharedConfigurationOnReload() {
		var eventBus = Mockito.mock(EventBus.class);
		var source = Mockito.mock(ConfigSource.class);
		var initial = Map.<String, Object>of(
				"blog",
				Map.of("detail", Map.of(
						"route", "/blog/{slug}",
						"template", "collections/blog.html")),
				"listing-only",
				Map.of());
		var updated = Map.<String, Object>of(
				"products",
				Map.of("detail", Map.of(
						"route", "/products/{id}",
						"template", "collections/product.html")));

		Mockito.when(source.exists()).thenReturn(true);
		Mockito.when(source.reload()).thenReturn(false, true);
		Mockito.when(source.getMap("collections")).thenReturn(initial, updated);

		var configuration = CollectionConfiguration.builder(eventBus)
				.id("collections")
				.addSource(source)
				.build();
		var sharedCollections = configuration.getCollections();

		Assertions.assertThat(sharedCollections).containsOnlyKeys("blog", "listing-only");
		Assertions.assertThat(sharedCollections.get("blog").detailPage().orElseThrow().parameter())
				.isEqualTo("slug");

		configuration.reload();

		Assertions.assertThat(configuration.getCollections()).isSameAs(sharedCollections);
		Assertions.assertThat(sharedCollections).containsOnlyKeys("products");
		Assertions.assertThat(sharedCollections.get("products").detailPage().orElseThrow().route())
				.isEqualTo("/products/{id}");
		Mockito.verify(eventBus).publish(new ConfigurationReloadEvent("collections"));
	}
}
