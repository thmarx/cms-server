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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class VFSTest {

	@BeforeAll
	public static void setup() {
		VFS.register(new RootFileSystemProvider("site", (siteContext) -> Path.of("src/test/resources")));
		VFS.register(new RootFileSystemProvider("content", (siteContext) -> Path.of("src/test/resources/content")));
	}

	@Test
	public void testSiteScheme() {
		ScopedValue.where(SiteContext.SCOPE, new SiteContext("demo-site", null, null)).run(() -> {
			var sitePath = VFS.resolve("site://");
			Assertions.assertThat(sitePath).exists();

			var content = VFS.resolve("site://content/");
			Assertions.assertThat(content).exists();
		});
	}

	@Test
	public void testContentScheme() {
		ScopedValue.where(SiteContext.SCOPE, new SiteContext("demo-site", null, null)).run(() -> {
			var sitePath = VFS.resolve("content://");
			Assertions.assertThat(sitePath).exists();

			var content = VFS.resolve("content://index.md");
			Assertions.assertThat(content).exists();
		});
	}
}
