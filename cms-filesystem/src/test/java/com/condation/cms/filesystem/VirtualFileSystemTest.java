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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

public class VirtualFileSystemTest {

	@TempDir
	Path tempDir;

	private VirtualFileSystem vfs;

	@BeforeEach
	public void setup() {
		vfs = VirtualFileSystem.createDefault(tempDir);
	}

	@Test
	public void testResolveValidPaths() {
		Path resolved = vfs.resolve("assets://images/logo.png");
		Assertions.assertThat(resolved).isEqualTo(tempDir.resolve("assets/images/logo.png").toAbsolutePath().normalize());

		resolved = vfs.resolve("content://pages/index.md");
		Assertions.assertThat(resolved).isEqualTo(tempDir.resolve("content/pages/index.md").toAbsolutePath().normalize());
	}

	@Test
	public void testUnknownSchema() {
		Assertions.assertThatThrownBy(() -> vfs.resolve("unknown://file.txt"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Unknown schema");
	}

	@Test
	public void testInvalidFormat() {
		Assertions.assertThatThrownBy(() -> vfs.resolve("assets:file.txt"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid virtual path format");
	}

	@Test
	public void testDirectoryTraversalPrevention() {
		// Attempt to go outside of assets using ../
		Assertions.assertThatThrownBy(() -> vfs.resolve("assets://../config/secret.toml"))
				.isInstanceOf(SecurityException.class)
				.hasMessageContaining("Path traversal attempt detected");
		
		// Attempt to go to /etc/passwd
		Assertions.assertThatThrownBy(() -> vfs.resolve("assets://../../../../etc/passwd"))
				.isInstanceOf(SecurityException.class)
				.hasMessageContaining("Path traversal attempt detected");
	}

	@Test
	public void testCustomSchemas() {
		VirtualFileSystem customVfs = new VirtualFileSystem(tempDir, Map.of("data", "custom-data-dir"));
		
		Path resolved = customVfs.resolve("data://file.json");
		Assertions.assertThat(resolved).isEqualTo(tempDir.resolve("custom-data-dir/file.json").toAbsolutePath().normalize());
	}
	
	@Test
	public void testSecurityOnCreation() {
		// Schema root trying to escape base path
		Assertions.assertThatThrownBy(() -> new VirtualFileSystem(tempDir, Map.of("bad", "../outside")))
				.isInstanceOf(SecurityException.class)
				.hasMessageContaining("outside of base path");
	}
}
