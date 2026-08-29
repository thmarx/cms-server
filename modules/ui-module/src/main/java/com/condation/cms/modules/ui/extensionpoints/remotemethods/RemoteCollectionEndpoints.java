package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.content.template.functions.LinkFunction;
import com.condation.cms.core.content.io.ContentFileParser;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.cms.modules.ui.utils.FormHelper;
import com.condation.cms.modules.ui.utils.MetaConverter;
import com.condation.cms.modules.ui.utils.NumberUtils;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/** Manager endpoints for listing and editing collection items. */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteCollectionEndpoints extends AbstractRemoteMethodeExtension {

	private static final long DEFAULT_PAGE_SIZE = 10;
	private static final long MAX_PAGE_SIZE = 100;
	private static final Pattern ITEM_ID = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_.-]*");

	public record ItemDto(
			String id,
			String collection,
			String path,
			String title,
			String detailUrl,
			Map<String, Object> meta) {
	}

	public record EditableItemDto(
			String id,
			String collection,
			String path,
			String content,
			Map<String, Object> meta) {
	}

	@RemoteMethod(name = "collections.items", permissions = {Permissions.CONTENT_EDIT})
	public Object items(Map<String, Object> parameters) throws RPCException {
		var db = getDB(parameters);
		var collectionName = requiredString(parameters, Parameters.COLLECTION);
		ensureCollectionExists(db.getCollections().names(), collectionName);

		long page = Math.max(1, NumberUtils.toLong(parameters.getOrDefault("page", 1L)));
		long size = Math.clamp(
				NumberUtils.toLong(parameters.getOrDefault("size", DEFAULT_PAGE_SIZE)),
				1,
				MAX_PAGE_SIZE);
		var title = optionalString(parameters, "query");

		var query = db.getCollections().collection(collectionName).query();
		if (!title.isBlank()) {
			query.searchByTitle(title);
		}
		query.orderby(Constants.MetaFields.TITLE).asc();
		Page<CollectionItem> result = query.page(page, size);
		return new Page<>(
				result.getTotalItems(),
				result.getPageSize(),
				result.getTotalPages(),
				result.getPage(),
				result.getItems().stream().map(this::itemDto).toList());
	}


	@RemoteMethod(name = "collections.item.get", permissions = {Permissions.CONTENT_EDIT})
	public Object get(Map<String, Object> parameters) throws RPCException {
		var item = item(parameters);
		return new EditableItemDto(
				item.id(),
				item.collection(),
				item.path(),
				item.content(),
				item.meta());
	}

	@RemoteMethod(name = "collections.item.save", permissions = {Permissions.CONTENT_EDIT})
	public Object save(Map<String, Object> parameters) throws RPCException {
		var db = getDB(parameters);
		var item = item(parameters);
		ensureLocalCollection(db, item.collection());
		var sourceFile = db.getFileSystem().collectionsBase().resolve(item.path());
		var writableFile = db.getFileSystem().resolve(Constants.Folders.COLLECTIONS).resolve(item.path());
		try {
			var parser = new ContentFileParser(sourceFile);
			var meta = new HashMap<>(parser.getHeader());
			var rawMeta = typedMeta(parameters.get("meta"));
			YamlHeaderUpdater.mergeFlatMapIntoNestedMap(meta, MetaConverter.convertMeta(rawMeta));
			var content = parameters.containsKey(Parameters.CONTENT)
					? FormHelper.getContent(parameters.get(Parameters.CONTENT))
					: parser.getContent();
			YamlHeaderUpdater.saveMarkdownFileWithHeader(writableFile, meta, content);
			db.getCollections().refresh(item.collection(), item.id());
			invalidateContentCache();
			return Map.of("saved", true);
		} catch (IOException | RuntimeException ex) {
			log.error("could not save collection item {}/{}", item.collection(), item.id(), ex);
			throw new RPCException(0, ex.getMessage());
		}
	}

	@RemoteMethod(name = "collections.item.create", permissions = {Permissions.CONTENT_EDIT})
	public Object create(Map<String, Object> parameters) throws RPCException {
		var db = getDB(parameters);
		var collectionName = requiredString(parameters, Parameters.COLLECTION);
		var id = requiredItemId(parameters);
		ensureCollectionExists(db.getCollections().names(), collectionName);
		ensureLocalCollection(db, collectionName);
		var writableFile = writableFile(db, collectionName, id);
		if (Files.exists(writableFile)) {
			throw new RPCException(409, "collection item already exists");
		}

		var meta = new HashMap<String, Object>();
		YamlHeaderUpdater.mergeFlatMapIntoNestedMap(
				meta,
				MetaConverter.convertMeta(typedMeta(parameters.get("meta"))));
		meta.putIfAbsent(Constants.MetaFields.TITLE, id);
		meta.put("createdAt", Date.from(Instant.now()));
		meta.put("createdBy", getUserName());
		meta.put(
				Constants.MetaFields.STATUS,
				getContext().get(WorkflowFeature.class).workflow().getStatusProvider().newNodeStatus());
		var content = FormHelper.getContent(parameters.get(Parameters.CONTENT));

		try {
			Files.createDirectories(writableFile.getParent());
			YamlHeaderUpdater.saveMarkdownFileWithHeader(writableFile, meta, content);
			db.getCollections().refresh(collectionName, id);
			invalidateContentCache();
			return itemDto(new CollectionItem(
					id,
					collectionName,
					collectionName + "/" + id + ".md",
					content,
					meta));
		} catch (IOException | RuntimeException exception) {
			log.error("could not create collection item {}/{}", collectionName, id, exception);
			throw new RPCException(0, exception.getMessage());
		}
	}

	@RemoteMethod(name = "collections.item.delete", permissions = {Permissions.CONTENT_EDIT})
	public Object delete(Map<String, Object> parameters) throws RPCException {
		var db = getDB(parameters);
		var collectionName = requiredString(parameters, Parameters.COLLECTION);
		var id = requiredItemId(parameters);
		ensureCollectionExists(db.getCollections().names(), collectionName);
		ensureLocalCollection(db, collectionName);
		var writableFile = writableFile(db, collectionName, id);
		if (!Files.isRegularFile(writableFile)) {
			throw new RPCException(404, "collection item not found");
		}

		try {
			Files.delete(writableFile);
			db.getCollections().refresh(collectionName, id);
			invalidateContentCache();
			return Map.of("deleted", true);
		} catch (IOException | RuntimeException exception) {
			log.error("could not delete collection item {}/{}", collectionName, id, exception);
			throw new RPCException(0, exception.getMessage());
		}
	}

	private CollectionItem item(Map<String, Object> parameters) throws RPCException {
		var db = getDB(parameters);
		var collectionName = requiredString(parameters, Parameters.COLLECTION);
		var id = requiredString(parameters, "id");
		ensureCollectionExists(db.getCollections().names(), collectionName);
		try {
			return db.getCollections().collection(collectionName).item(id)
					.orElseThrow(() -> new RPCException(404, "collection item not found"));
		} catch (IllegalArgumentException ex) {
			throw new RPCException(400, ex.getMessage());
		}
	}

	private ItemDto itemDto(CollectionItem item) {
		var title = item.meta().get(Constants.MetaFields.TITLE);
		return new ItemDto(
				item.id(),
				item.collection(),
				item.path(),
				title == null || title.toString().isBlank() ? item.id() : title.toString(),
				detailUrl(item),
				item.meta());
	}

	private String detailUrl(CollectionItem item) {
		try {
			return new LinkFunction(getRequestContext()).collectionUrl(item);
		} catch (IllegalArgumentException | IllegalStateException _) {
			return null;
		}
	}

	private void invalidateContentCache() {
		getContext().get(EventBusFeature.class).eventBus().publish(new InvalidateContentCacheEvent());
	}

	private static Path writableFile(DB db, String collectionName, String id) {
		return db.getFileSystem().resolve(Constants.Folders.COLLECTIONS)
				.resolve(collectionName)
				.resolve(id + ".md");
	}

	private static void ensureCollectionExists(java.util.Set<String> names, String name) throws RPCException {
		if (!names.contains(name)) {
			throw new RPCException(404, "collection not found: " + name);
		}
	}

	private static void ensureLocalCollection(DB db, String name) throws RPCException {
		if (!db.getCollections().isLocal(name)) {
			throw new RPCException(403, "referenced collection is read-only: " + name);
		}
	}

	private static String requiredString(Map<String, Object> parameters, String name) throws RPCException {
		var value = optionalString(parameters, name);
		if (value.isBlank()) {
			throw new RPCException(400, name + " must not be blank");
		}
		return value;
	}

	private static String requiredItemId(Map<String, Object> parameters) throws RPCException {
		var id = requiredString(parameters, "id");
		if (!ITEM_ID.matcher(id).matches()) {
			throw new RPCException(400, "invalid collection item id");
		}
		return id;
	}

	private static String optionalString(Map<String, Object> parameters, String name) {
		return parameters.get(name) instanceof String value ? value.trim() : "";
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Map<String, Object>> typedMeta(Object value) {
		return value instanceof Map<?, ?> map
				? (Map<String, Map<String, Object>>) map
				: Map.of();
	}
}
