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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.content.utils.SlugUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class LinkFunction {

	private final RequestContext requestContext;
	
	public String createUrl (String url) {
		return HTTPUtil.modifyUrl(url, requestContext);
	}

	/**
	 * Creates the configured detail URL for a collection item.
	 *
	 * @param item collection item to link to
	 * @return context-aware detail URL
	 */
	public String collectionUrl(CollectionItem item) {
		Objects.requireNonNull(item, "collection item must not be null");
		var configuration = requestContext.get(ConfigurationFeature.class)
				.configuration()
				.get(CollectionConfiguration.class);
		if (configuration == null) {
			throw new IllegalStateException("collection configuration is not available");
		}

		var definition = configuration.collection(item.collection())
				.orElseThrow(() -> new IllegalArgumentException(
						"collection is not configured: " + item.collection()));
		var detail = definition.detailPage()
				.orElseThrow(() -> new IllegalArgumentException(
						"collection has no detail route: " + item.collection()));
		var idParameter = "id".equals(detail.parameter());
		var parameterValue = idParameter
				? item.id()
				: MapUtil.getValue(item.meta(), detail.parameter());
		if (parameterValue == null || parameterValue.toString().isBlank()) {
			throw new IllegalArgumentException(
					"collection item has no route value for: " + detail.parameter());
		}

		var routeValue = idParameter
				? parameterValue.toString()
				: SlugUtil.slugify(parameterValue.toString());
		if (routeValue.isBlank()) {
			throw new IllegalArgumentException(
					"collection item route value cannot be converted to a slug: " + detail.parameter());
		}

		var route = detail.route().replace(
				"{" + detail.parameter() + "}",
				routeValue);
		return createUrl(route);
	}
}
