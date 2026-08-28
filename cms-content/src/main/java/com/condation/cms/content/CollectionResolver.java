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

import com.condation.cms.api.Constants;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/**
 * Resolves configured collection detail routes and renders their items.
 */
@RequiredArgsConstructor
public class CollectionResolver {

	private final ContentRenderer contentRenderer;
	private final DB db;
	private final Configuration configuration;

	public Optional<ContentResponse> getContent(RequestContext context) throws IOException {
		var collectionConfiguration = configuration.get(CollectionConfiguration.class);
		if (collectionConfiguration == null) {
			return Optional.empty();
		}

		var uri = normalizeUri(context.get(RequestFeature.class).uri());
		for (var definition : collectionConfiguration.collections().values().stream()
				.sorted(Comparator.comparing(CollectionDefinition::name))
				.toList()) {
			var detail = definition.detailPage();
			if (detail.isEmpty()) {
				continue;
			}
			var routeValue = match(detail.get(), uri);
			if (routeValue.isEmpty()) {
				continue;
			}
			return resolve(definition, detail.get(), routeValue.get(), uri, context);
		}
		return Optional.empty();
	}

	private Optional<ContentResponse> resolve(
			CollectionDefinition definition,
			CollectionDetailConfiguration detail,
			String routeValue,
			String uri,
			RequestContext context) throws IOException {
		var collection = db.getCollections().collection(definition.name());
		Optional<CollectionItem> item;
		if ("id".equals(detail.parameter())) {
			item = findById(collection, routeValue);
		} else {
			item = collection.query()
					.where(detail.parameter(), routeValue)
					.page(1, 1)
					.getItems()
					.stream()
					.findFirst();
		}
		if (item.isEmpty()) {
			return Optional.empty();
		}

		var collectionItem = item.get();
		var nodeData = new HashMap<>(collectionItem.meta());
		nodeData.put("template", detail.template());
		var node = new ContentNode(
				collectionItem.path(),
				uri,
				collectionItem.id() + ".md",
				nodeData);
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(node));

		var collectionFile = db.getFileSystem().collectionsBase().resolve(collectionItem.path());
		if (!collectionFile.exists()) {
			return Optional.empty();
		}
		var content = contentRenderer.renderCollection(
				collectionFile,
				node,
				collectionItem,
				detail.template(),
				context);
		return Optional.of(new DefaultContentResponse(content, Constants.DEFAULT_CONTENT_TYPE, node));
	}

	private static Optional<CollectionItem> findById(
			com.condation.cms.api.db.collection.Collection collection,
			String id) {
		try {
			return collection.item(id);
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private static Optional<String> match(CollectionDetailConfiguration detail, String uri) {
		var token = "{" + detail.parameter() + "}";
		var tokenStart = detail.route().indexOf(token);
		var prefix = detail.route().substring(0, tokenStart);
		var suffix = detail.route().substring(tokenStart + token.length());
		var routePattern = Pattern.compile(
				"^" + Pattern.quote(prefix) + "([^/]+)" + Pattern.quote(suffix) + "/?$");
		var matcher = routePattern.matcher(uri);
		return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
	}

	private static String normalizeUri(String uri) {
		var normalized = uri == null ? "" : uri.trim();
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		return normalized;
	}
}
