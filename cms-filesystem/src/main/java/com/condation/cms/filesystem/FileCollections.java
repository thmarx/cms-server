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
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.filesystem.metadata.persistent.CollectionMetaData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Site-scoped, file-backed collections implementation.
 */
@Slf4j
public class FileCollections implements Collections, AutoCloseable {

	private static final Pattern COLLECTION_NAME = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_-]*");
	private static final Duration CHANGE_QUIET_PERIOD = Duration.ofMillis(200);

	private final String siteId;
	private final Path hostBase;
	private final Path collectionsBase;
	private final Function<Path, Map<String, Object>> metaParser;
	private final Set<String> collectionNames = ConcurrentHashMap.newKeySet();

	private CollectionMetaData metaData;
	private MultiRootRecursiveWatcher watcher;
	private ContentChangeCoordinator changeCoordinator;

	public FileCollections(
			String siteId,
			Path hostBase,
			Function<Path, Map<String, Object>> metaParser) {
		this.siteId = siteId;
		this.hostBase = hostBase;
		this.collectionsBase = hostBase.resolve(Constants.Folders.COLLECTIONS);
		this.metaParser = metaParser;
	}

	public void init() throws IOException {
		Files.createDirectories(collectionsBase);
		metaData = new CollectionMetaData(hostBase);
		metaData.open();
		changeCoordinator = new ContentChangeCoordinator(
				CHANGE_QUIET_PERIOD,
				this::processChanges);
		rebuild();

		watcher = new MultiRootRecursiveWatcher(siteId, List.of(collectionsBase));
		var publisher = Objects.requireNonNull(
				watcher.getPublisher(collectionsBase),
				"collections publisher must be available");
		publisher.subscribe(
				new MultiRootRecursiveWatcher.AbstractFileEventSubscriber() {
					@Override
					public void onNext(FileEvent item) {
						if (item.type() == FileEvent.Type.OVERFLOW) {
							changeCoordinator.requestFullResync();
						} else {
							changeCoordinator.submit(item.file().toPath());
						}
						this.subscription.request(1);
					}
				});
		watcher.start();
	}

	@Override
	public com.condation.cms.api.db.collection.Collection collection(String name) {
		validateCollectionName(name);
		return new FileCollection(name);
	}

	@Override
	public Set<String> names() {
		return Set.copyOf(collectionNames);
	}

	void handleEvent(FileEvent event) {
		if (event.type() == FileEvent.Type.OVERFLOW) {
			changeCoordinator.requestFullResync();
		} else {
			changeCoordinator.submit(event.file().toPath());
		}
	}

	void flushChanges() {
		changeCoordinator.flushNow();
	}

	private void processChanges(boolean fullResync, Set<Path> paths) {
		try {
			if (fullResync) {
				rebuild();
				return;
			}
			for (var path : paths) {
				processPath(path);
			}
		} catch (IOException ex) {
			log.error("error processing collection changes", ex);
		}
	}

	private void processPath(Path path) throws IOException {
		var relative = PathUtil.toRelativeEntry(path, collectionsBase);
		if (relative.isBlank() || relative.startsWith("../")) {
			return;
		}
		var parts = relative.split("/");
		if (parts.length == 1) {
			if (Files.isDirectory(path)) {
				scanCollection(path);
			} else if (!Files.exists(path)) {
				metaData.removeDirectory(parts[0]);
				collectionNames.remove(parts[0]);
			}
			return;
		}
		if (parts.length != 2 || !isMarkdown(path)) {
			return;
		}
		if (!isValidCollectionName(parts[0])) {
			return;
		}
		if (Files.isRegularFile(path)) {
			index(path);
		} else if (!Files.exists(path)) {
			metaData.removeFile(relative);
		}
	}

	private void rebuild() throws IOException {
		metaData.clear();
		collectionNames.clear();
		metaData.startBatch();
		try (var collections = Files.list(collectionsBase)) {
			for (var collection : collections.filter(Files::isDirectory).toList()) {
				scanCollection(collection);
			}
		} finally {
			metaData.stopBatch();
		}
	}

	private void scanCollection(Path collection) throws IOException {
		var name = collection.getFileName().toString();
		if (!isValidCollectionName(name)) {
			log.warn("ignoring invalid collection name {}", name);
			return;
		}
		collectionNames.add(name);
		metaData.removeDirectory(name);
		try (var files = Files.list(collection)) {
			for (var file : files.filter(Files::isRegularFile).filter(FileCollections::isMarkdown).toList()) {
				index(file);
			}
		}
	}

	private void index(Path file) throws IOException {
		var path = PathUtil.toRelativeEntry(file, collectionsBase);
		collectionNames.add(path.substring(0, path.indexOf('/')));
		var modified = LocalDate.ofInstant(
				Files.getLastModifiedTime(file).toInstant(),
				ZoneId.systemDefault());
		metaData.addFile(path, metaParser.apply(file), modified);
	}

	private CollectionItem map(ContentNode node, int ignoredExcerptLength) {
		var path = node.path();
		var separator = path.indexOf('/');
		var collection = path.substring(0, separator);
		var filename = path.substring(separator + 1);
		var id = filename.substring(0, filename.length() - 3);
		return new CollectionItem(
				id,
				collection,
				path,
				readMarkdownBody(collectionsBase.resolve(path)),
				node.data());
	}

	private static String readMarkdownBody(Path file) {
		try {
			var lines = Files.readAllLines(file);
			var body = new StringBuilder();
			var inFrontMatter = false;
			var frontMatterClosed = false;
			for (var line : lines) {
				if (line.trim().equals("---") && !frontMatterClosed) {
					if (!inFrontMatter) {
						inFrontMatter = true;
					} else {
						inFrontMatter = false;
						frontMatterClosed = true;
					}
					continue;
				}
				if (!inFrontMatter) {
					body.append(line).append("\r\n");
				}
			}
			return body.toString();
		} catch (IOException ex) {
			log.error("error reading collection item {}", file, ex);
			return "";
		}
	}

	private static boolean isMarkdown(Path file) {
		return file.getFileName().toString().endsWith(".md");
	}

	private static boolean isValidCollectionName(String name) {
		return name != null && COLLECTION_NAME.matcher(name).matches();
	}

	private static void validateCollectionName(String name) {
		if (!isValidCollectionName(name)) {
			throw new IllegalArgumentException("invalid collection name: " + name);
		}
	}

	@Override
	public void close() throws IOException {
		if (watcher != null) {
			watcher.stop();
		}
		if (changeCoordinator != null) {
			changeCoordinator.close();
		}
		if (metaData != null) {
			metaData.close();
		}
	}

	private class FileCollection implements com.condation.cms.api.db.collection.Collection {

		private final String name;

		private FileCollection(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public ContentQuery<CollectionItem> query() {
			return metaData.query(name, FileCollections.this::map);
		}
	}
}
