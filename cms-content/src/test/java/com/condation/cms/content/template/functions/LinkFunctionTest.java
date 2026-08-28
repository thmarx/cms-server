package com.condation.cms.content.template.functions;

/*-
 * #%L
 * CMS Content
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkFunctionTest {

	private final ConcurrentHashMap<String, CollectionDefinition> definitions = new ConcurrentHashMap<>();
	private final RequestContext context = new RequestContext();
	private final CollectionItem item = new CollectionItem(
			"item_1",
			"blog",
			"blog/item_1.md",
			"",
			Map.of("slug", "first-post"));

	@BeforeEach
	void setUp() {
		var configuration = new Configuration();
		configuration.add(CollectionConfiguration.class, new CollectionConfiguration(definitions));
		context.add(ConfigurationFeature.class, new ConfigurationFeature(configuration));

		var siteProperties = mock(SiteProperties.class);
		when(siteProperties.contextPath()).thenReturn("/docs");
		context.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
	}

	@Test
	void createsContextAwareUrlUsingTheItemId() {
		definitions.put("blog", definition("/articles/{id}"));

		var url = new LinkFunction(context).collectionUrl(item);

		Assertions.assertThat(url).isEqualTo("/docs/articles/item_1");
	}

	@Test
	void createsContextAwareUrlUsingConfiguredFrontMatter() {
		definitions.put("blog", definition("/articles/{slug}"));

		var url = new LinkFunction(context).collectionUrl(item);

		Assertions.assertThat(url).isEqualTo("/docs/articles/first-post");
	}

	@Test
	void usesReloadedCollectionRoute() {
		definitions.put("blog", definition("/old/{id}"));
		var links = new LinkFunction(context);
		Assertions.assertThat(links.collectionUrl(item)).isEqualTo("/docs/old/item_1");

		definitions.put("blog", definition("/new/{slug}"));

		Assertions.assertThat(links.collectionUrl(item)).isEqualTo("/docs/new/first-post");
	}

	@Test
	void rejectsItemsWithoutTheConfiguredRouteValue() {
		definitions.put("blog", definition("/articles/{slug}"));
		var itemWithoutSlug = new CollectionItem(
				"item_2",
				"blog",
				"blog/item_2.md",
				"",
				Map.of());

		Assertions.assertThatIllegalArgumentException()
				.isThrownBy(() -> new LinkFunction(context).collectionUrl(itemWithoutSlug))
				.withMessageContaining("slug");
	}

	private static CollectionDefinition definition(String route) {
		return new CollectionDefinition(
				"blog",
				new CollectionDetailConfiguration(route, "collections/detail.html"));
	}
}
