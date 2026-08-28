package com.condation.cms.content;

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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CollectionResolverTest {

	private final ContentRenderer renderer = mock(ContentRenderer.class);
	private final DB db = mock(DB.class);
	private final com.condation.cms.api.db.collection.Collections collections =
			mock(com.condation.cms.api.db.collection.Collections.class);
	private final Collection collection = mock(Collection.class);
	private final DBFileSystem fileSystem = mock(DBFileSystem.class);
	private final ReadOnlyFile collectionsBase = mock(ReadOnlyFile.class);
	private final ReadOnlyFile itemFile = mock(ReadOnlyFile.class);
	private final ConcurrentHashMap<String, CollectionDefinition> definitions = new ConcurrentHashMap<>();
	private final Configuration configuration = new Configuration();
	private final CollectionItem item = new CollectionItem(
			"first",
			"blog",
			"blog/first.md",
			"# First",
			Map.of("title", "First", "slug", "first-post"));

	@BeforeEach
	void setUp() throws Exception {
		configuration.add(CollectionConfiguration.class, new CollectionConfiguration(definitions));
		when(db.getCollections()).thenReturn(collections);
		when(collections.collection("blog")).thenReturn(collection);
		when(db.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.collectionsBase()).thenReturn(collectionsBase);
		when(collectionsBase.resolve("blog/first.md")).thenReturn(itemFile);
		when(itemFile.exists()).thenReturn(true);
		when(renderer.renderCollection(
				eq(itemFile),
				any(),
				eq(item),
				anyString(),
				any())).thenReturn("<h1>First</h1>");
	}

	@Test
	void resolvesAnIdRouteAndUsesReloadedDefinitions() throws Exception {
		definitions.put("blog", definition("/old/{id}"));
		when(collection.item("first")).thenReturn(Optional.of(item));
		var resolver = new CollectionResolver(renderer, db, configuration);
		var context = context("/blog/first");

		Assertions.assertThat(resolver.getContent(context)).isEmpty();

		definitions.put("blog", definition("/blog/{id}"));
		var response = resolver.getContent(context);

		Assertions.assertThat(response)
				.isPresent()
				.get()
				.isInstanceOfSatisfying(DefaultContentResponse.class, content ->
						Assertions.assertThat(content.content()).isEqualTo("<h1>First</h1>"));
		Assertions.assertThat(context.get(CurrentNodeFeature.class).node().url()).isEqualTo("/blog/first");
		var node = ArgumentCaptor.forClass(com.condation.cms.api.db.ContentNode.class);
		verify(renderer).renderCollection(
				eq(itemFile),
				node.capture(),
				eq(item),
				eq("collections/detail.html"),
				eq(context));
		Assertions.assertThat(node.getValue().data()).containsEntry("template", "collections/detail.html");
	}

	@Test
	void resolvesAConfiguredFrontMatterField() throws Exception {
		definitions.put("blog", definition("/blog/{slug}"));
		@SuppressWarnings("unchecked")
		var query = (ContentQuery<CollectionItem>) mock(ContentQuery.class);
		when(collection.query()).thenReturn(query);
		when(query.where("slug", "first-post")).thenReturn(query);
		when(query.page(1, 1)).thenReturn(new Page<>(1, 1, 1, 1, List.of(item)));
		var resolver = new CollectionResolver(renderer, db, configuration);

		var response = resolver.getContent(context("/blog/first-post/"));

		Assertions.assertThat(response).isPresent();
		verify(query).where("slug", "first-post");
	}

	private static CollectionDefinition definition(String route) {
		return new CollectionDefinition(
				"blog",
				new CollectionDetailConfiguration(route, "collections/detail.html"));
	}

	private static RequestContext context(String uri) {
		var context = new RequestContext();
		context.add(RequestFeature.class, new RequestFeature(uri, Map.of()));
		return context;
	}
}
