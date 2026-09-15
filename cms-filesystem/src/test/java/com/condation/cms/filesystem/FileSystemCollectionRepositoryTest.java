package com.condation.cms.filesystem;

/*-
 * #%L
 * CMS FileSystem
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.api.repository.CollectionAccess;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemCollectionRepositoryTest {

	@TempDir
	Path tempDirectory;

	private Collections collections;
	private FileSystemCollectionRepository repository;

	@BeforeEach
	void setUp() {
		collections = mock(Collections.class);
		var fileSystem = mock(DBFileSystem.class);
		when(collections.names()).thenReturn(Set.of("blog", "shared"));
		when(collections.isLocal("blog")).thenReturn(true);
		when(collections.isLocal("shared")).thenReturn(false);
		when(fileSystem.resolve(Constants.Folders.COLLECTIONS))
				.thenReturn(tempDirectory.resolve("collections"));
		repository = new FileSystemCollectionRepository(collections, fileSystem);
	}

	@Test
	void createsSavesAndDeletesItemsBehindTheRepositoryBoundary() throws Exception {
		repository.create("blog", "entry", Map.of("title", "First"), "First body");
		var item = tempDirectory.resolve("collections/blog/entry.md");
		Assertions.assertThat(item)
				.exists()
				.content()
				.contains("title: First", "First body");
		verify(collections).refresh("blog", "entry");

		repository.save("blog", "entry", Map.of("title", "Changed"), "Changed body");
		Assertions.assertThat(item).content().contains("title: Changed", "Changed body");

		repository.delete("blog", "entry");
		Assertions.assertThat(item).doesNotExist();
		verify(collections, org.mockito.Mockito.times(3)).refresh("blog", "entry");
	}

	@Test
	void rejectsOverwritesTraversalAndWritesToReferencedCollections() throws Exception {
		repository.create("blog", "entry", Map.of(), "body");

		Assertions.assertThatThrownBy(
				() -> repository.create("blog", "entry", Map.of(), "other"))
				.isInstanceOf(FileAlreadyExistsException.class);
		Assertions.assertThatThrownBy(
				() -> repository.save("blog", "../entry", Map.of(), "other"))
				.isInstanceOf(IllegalArgumentException.class);
		Assertions.assertThatThrownBy(
				() -> repository.save("shared", "entry", Map.of(), "other"))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("read-only");
		Assertions.assertThat(repository.access("blog")).isEqualTo(CollectionAccess.READ_WRITE);
		Assertions.assertThat(repository.access("shared")).isEqualTo(CollectionAccess.READ_ONLY);
	}
}
