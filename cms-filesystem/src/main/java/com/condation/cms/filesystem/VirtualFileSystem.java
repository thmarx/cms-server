package com.condation.cms.filesystem;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Virtual File System that resolves schema-based paths (e.g., assets://, content://)
 * to absolute file system paths relative to a base directory.
 * 
 * It includes security checks to prevent directory traversal attacks.
 * 
 * @author t.marx
 */
public class VirtualFileSystem {

	private final Path basePath;
	private final Map<String, Path> schemaRoots;

	/**
	 * Creates a new VirtualFileSystem with a base path and a map of schemas to subdirectories.
	 * 
	 * @param basePath the root directory for this VFS instance
	 * @param schemas a map where keys are schemas (e.g., "assets") and values are 
	 *                relative subdirectory names (e.g., "assets")
	 */
	public VirtualFileSystem(Path basePath, Map<String, String> schemas) {
		this.basePath = basePath.toAbsolutePath().normalize();
		Map<String, Path> roots = new HashMap<>();
		for (Map.Entry<String, String> entry : schemas.entrySet()) {
			Path schemaRoot = this.basePath.resolve(entry.getValue()).toAbsolutePath().normalize();
			if (!schemaRoot.startsWith(this.basePath)) {
				throw new SecurityException("Schema root '" + entry.getValue() + "' is outside of base path");
			}
			roots.put(entry.getKey(), schemaRoot);
		}
		this.schemaRoots = Collections.unmodifiableMap(roots);
	}

	/**
	 * Resolves a virtual path to an absolute Path on the file system.
	 * 
	 * Example: resolve("assets://images/logo.png") -> /base/path/assets/images/logo.png
	 * 
	 * @param virtualPath the path to resolve (must start with "schema://")
	 * @return the resolved absolute Path
	 * @throws IllegalArgumentException if the schema is unknown or the format is invalid
	 * @throws SecurityException if the resolved path would escape its schema root
	 */
	public Path resolve(String virtualPath) {
		int schemaEnd = virtualPath.indexOf("://");
		if (schemaEnd == -1) {
			throw new IllegalArgumentException("Invalid virtual path format (missing ://): " + virtualPath);
		}

		String schema = virtualPath.substring(0, schemaEnd);
		String relativePath = virtualPath.substring(schemaEnd + 3);

		Path root = schemaRoots.get(schema);
		if (root == null) {
			throw new IllegalArgumentException("Unknown schema: " + schema);
		}

		// Resolve and normalize the path
		Path resolved = root.resolve(relativePath).toAbsolutePath().normalize();

		// Security check: ensure the resolved path is still within the schema root
		if (!resolved.startsWith(root)) {
			throw new SecurityException("Path traversal attempt detected: " + virtualPath);
		}

		return resolved;
	}

	/**
	 * Helper method to create a VFS with the standard CMS schemas.
	 * 
	 * @param basePath the host base directory
	 * @return a pre-configured VirtualFileSystem
	 */
	public static VirtualFileSystem createDefault(Path basePath) {
		Map<String, String> schemas = new HashMap<>();
		schemas.put("assets", "assets");
		schemas.put("content", "content");
		schemas.put("config", "config");
		schemas.put("extensions", "extensions");
		return new VirtualFileSystem(basePath, schemas);
	}
	
	public Map<String, Path> getSchemaRoots() {
		return schemaRoots;
	}
}
