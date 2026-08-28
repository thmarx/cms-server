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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Route and template used for collection detail pages.
 */
public record CollectionDetailConfiguration(String route, String template) {

	private static final Pattern PARAMETER = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_.-]*)}");

	public CollectionDetailConfiguration {
		Objects.requireNonNull(route, "collection detail route must not be null");
		Objects.requireNonNull(template, "collection detail template must not be null");

		route = normalizeRoute(route);
		template = template.trim();
		if (template.isEmpty()) {
			throw new IllegalArgumentException("collection detail template must not be blank");
		}

		var matcher = PARAMETER.matcher(route);
		if (!matcher.find() || matcher.find()) {
			throw new IllegalArgumentException("collection detail route must contain exactly one parameter");
		}
	}

	public String parameter() {
		var matcher = PARAMETER.matcher(route);
		if (!matcher.find()) {
			throw new IllegalStateException("collection detail route has no parameter");
		}
		return matcher.group(1);
	}

	private static String normalizeRoute(String value) {
		var normalized = value.trim();
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		while (normalized.length() > 1 && normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
