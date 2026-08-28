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

import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.collection.CollectionItem;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/** Resolves configured collection detail routes without rendering them. */
@RequiredArgsConstructor
public class CollectionRouteResolver {

	private final DB db;
	private final CollectionConfiguration configuration;

	public Optional<ResolvedRoute> resolve(String requestUri) {
		if (configuration == null) {
			return Optional.empty();
		}
		var uri = normalizeUri(requestUri);
		for (var definition : configuration.collections().values().stream()
				.sorted(Comparator.comparing(CollectionDefinition::name))
				.toList()) {
			var resolved = resolve(definition, uri);
			if (resolved.isPresent()) {
				return resolved;
			}
		}
		return Optional.empty();
	}

	private Optional<ResolvedRoute> resolve(CollectionDefinition definition, String uri) {
		var detail = definition.detailPage();
		if (detail.isEmpty()) {
			return Optional.empty();
		}
		var routeValue = match(detail.get(), uri);
		if (routeValue.isEmpty()) {
			return Optional.empty();
		}

		var collection = db.getCollections().collection(definition.name());
		Optional<CollectionItem> item;
		if ("id".equals(detail.get().parameter())) {
			item = findById(collection, routeValue.get());
		} else {
			item = collection.query()
					.where(detail.get().parameter(), routeValue.get())
					.page(1, 1)
					.getItems()
					.stream()
					.findFirst();
		}
		return item.map(value -> new ResolvedRoute(definition, detail.get(), value, uri));
	}

	private static Optional<CollectionItem> findById(
			com.condation.cms.api.db.collection.Collection collection,
			String id) {
		try {
			return collection.item(id);
		} catch (IllegalArgumentException _) {
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

	public record ResolvedRoute(
			CollectionDefinition definition,
			CollectionDetailConfiguration detail,
			CollectionItem item,
			String uri) {
	}
}
