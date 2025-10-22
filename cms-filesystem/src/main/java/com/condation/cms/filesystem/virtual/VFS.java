package com.condation.cms.filesystem.virtual;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class VFS {

	private static final Map<String, VirtualFileSystemProvider> providers = new HashMap<>();

	public static void register(VirtualFileSystemProvider provider) {
		providers.put(provider.getScheme(), provider);
	}

	public static Path resolve(String uri) {
		var site = SiteContext.SCOPE.get();

		String[] parts = uri.split("://", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid URI format: " + uri);
		}
		String scheme = parts[0];
		String path = parts[1];

		VirtualFileSystemProvider provider = providers.get(scheme);
		if (provider == null) {
			throw new IllegalArgumentException("No provider for scheme: " + scheme);
		}
		var resolved = provider.resolve(path, site);
		return resolved;
	}
}
