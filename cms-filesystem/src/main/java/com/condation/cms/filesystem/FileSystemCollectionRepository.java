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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.CursorPage;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.db.collection.CollectionCursorSupport;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.db.collection.CollectionItemId;
import com.condation.cms.api.db.collection.CollectionItemMetadata;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.api.repository.CollectionAccess;
import com.condation.cms.api.repository.MutableCollectionRepository;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/** Collection repository backed by the existing collection filesystem index. */
public final class FileSystemCollectionRepository implements MutableCollectionRepository {

	private final Collections collections;
	private final DBFileSystem fileSystem;

	public FileSystemCollectionRepository(Collections collections, DBFileSystem fileSystem) {
		this.collections = Objects.requireNonNull(collections);
		this.fileSystem = Objects.requireNonNull(fileSystem);
	}

	@Override
	public Set<String> names() {
		return collections.names();
	}

	@Override
	public CollectionAccess access(String collection) {
		validateCollection(collection);
		return collections.isLocal(collection)
				? CollectionAccess.READ_WRITE
				: CollectionAccess.READ_ONLY;
	}

	@Override
	public Collection collection(String name) {
		return collections.collection(name);
	}

	@Override
	public CursorPage<CollectionItemMetadata> metadataCursorPage(
			String collection,
			String cursor,
			long size,
			Consumer<ContentQuery<CollectionItemMetadata>> queryConfigurer) {
		if (!(collections instanceof CollectionCursorSupport cursorSupport)) {
			throw new UnsupportedOperationException("collection storage does not support cursor paging");
		}
		return cursorSupport.metadataCursorPage(collection, cursor, size, queryConfigurer);
	}

	@Override
	public CollectionItem create(
			String collection,
			String id,
			Map<String, Object> metadata,
			String content) throws IOException {
		var target = writableItem(collection, id);
		if (Files.exists(target)) {
			throw new FileAlreadyExistsException(collection + "/" + id);
		}
		write(collection, id, target, metadata, content);
		return new CollectionItem(
				id,
				collection,
				collection + "/" + id + ".md",
				content,
				metadata);
	}

	@Override
	public void save(
			String collection,
			String id,
			Map<String, Object> metadata,
			String content) throws IOException {
		write(collection, id, writableItem(collection, id), metadata, content);
	}

	@Override
	public void delete(String collection, String id) throws IOException {
		var target = writableItem(collection, id);
		if (!Files.isRegularFile(target)) {
			throw new java.nio.file.NoSuchFileException(collection + "/" + id);
		}
		Files.delete(target);
		collections.refresh(collection, id);
	}

	private void write(
			String collection,
			String id,
			Path target,
			Map<String, Object> metadata,
			String content) throws IOException {
		Files.createDirectories(target.getParent());
		YamlHeaderUpdater.saveMarkdownFileWithHeader(target, metadata, content);
		collections.refresh(collection, id);
	}

	private Path writableItem(String collection, String id) {
		validateCollection(collection);
		CollectionItemId.requireValid(id);
		if (access(collection) != CollectionAccess.READ_WRITE) {
			throw new UnsupportedOperationException("referenced collection is read-only: " + collection);
		}
		var root = fileSystem.resolve(Constants.Folders.COLLECTIONS).toAbsolutePath().normalize();
		var target = root.resolve(collection).resolve(id + ".md").normalize();
		if (!target.startsWith(root) || target.equals(root)) {
			throw new IllegalArgumentException("invalid collection item path");
		}
		return target;
	}

	private void validateCollection(String collection) {
		if (!collections.names().contains(collection)) {
			throw new IllegalArgumentException("collection not found: " + collection);
		}
	}
}
