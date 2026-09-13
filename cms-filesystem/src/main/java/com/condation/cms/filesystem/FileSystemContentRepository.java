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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.repository.ContentDocument;
import com.condation.cms.api.repository.ContentRepository;
import com.condation.cms.api.repository.ContentStore;
import com.condation.cms.api.repository.Section;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.api.variants.VariantContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ContentRepository} adapter backed by the existing filesystem index.
 *
 * @author thmar
 */
@Slf4j
public final class FileSystemContentRepository implements ContentRepository {

	private final Content content;
	private final DBFileSystem fileSystem;
	private final ContentStore contentStore;
	private final ContentParser contentParser;

	public FileSystemContentRepository(
			Content content,
			DBFileSystem fileSystem,
			ContentStore contentStore,
			ContentParser contentParser) {
		this.content = Objects.requireNonNull(content);
		this.fileSystem = Objects.requireNonNull(fileSystem);
		this.contentStore = Objects.requireNonNull(contentStore);
		this.contentParser = Objects.requireNonNull(contentParser);
	}

	@Override
	public Optional<ContentNode> get(String path) {
		return content.byPath(path);
	}

	@Override
	public Optional<ContentNode> findByUrl(String url) {
		return content.byUrl(url);
	}

	@Override
	public Optional<ContentDocument> load(ContentNode node) throws IOException {
		Objects.requireNonNull(node, "node");
		var resource = contentStore.get(node.path());
		if (resource.isEmpty() || resource.get().directory()) {
			return Optional.empty();
		}
		var parsedContent = contentParser.parse(resource.get());
		return Optional.of(new ContentDocument(node, parsedContent.content()));
	}

	@Override
	public List<ContentNode> children(String path) {
		return content.listContent(fileSystem.contentBase(), path);
	}

	@Override
	public List<ContentNode> directories(String path) {
		return content.listDirectories(fileSystem.contentBase(), path);
	}

	@Override
	public boolean isVisible(ContentNode node) {
		return content.isVisible(node);
	}

	@Override
	public List<Section> sections(ContentNode owner) throws IOException {
		Objects.requireNonNull(owner, "owner");
		return toSections(owner, sectionNodes(owner));
	}

	@Override
	public List<Section> sections(ContentNode owner, String sectionName) throws IOException {
		Objects.requireNonNull(owner, "owner");
		Objects.requireNonNull(sectionName, "sectionName");
		var matchingNodes = sectionNodes(owner).stream()
				.filter(section -> sectionName.equals(SectionUtil.getSectionName(section.name())))
				.toList();
		return toSections(owner, matchingNodes);
	}

	private List<ContentNode> sectionNodes(ContentNode owner) {
		return content.listSectionEntries(fileSystem.contentBase().resolve(owner.path()));
	}

	private List<Section> toSections(ContentNode owner, List<ContentNode> nodes) throws IOException {
		var sections = new ArrayList<Section>(nodes.size());
		for (var node : nodes) {
			var sectionResource = contentStore.get(node.path());
			if (sectionResource.isEmpty() || sectionResource.get().directory()) {
				continue;
			}
			var parsedContent = contentParser.parse(sectionResource.get());
			sections.add(new Section(
					node.path(),
					owner.path(),
					SectionUtil.getSectionName(node.name()),
					node.data(),
					parsedContent.content()));
		}
		return List.copyOf(sections);
	}

	@Override
	public List<Variant> variants(ContentNode node) {
		Objects.requireNonNull(node, "node");
		var canonical = canonicalNode(node);
		var parent = parentPath(canonical.path());
		var variantsPath = join(parent, ".variants/" + removeMarkdown(canonical.name()));

		try (var resources = contentStore.list(variantsPath)) {
			return resources
					.filter(resource -> resource.directory())
					.map(resource -> new VariantCandidate(
							fileName(resource.path()),
							join(resource.path(), canonical.name())))
					.map(candidate -> get(candidate.path())
							.map(variantNode -> new Variant(candidate.id(), variantNode)))
					.flatMap(Optional::stream)
					.toList();
		} catch (IOException ex) {
			log.error("error loading variants for {}", canonical.path(), ex);
			return Collections.emptyList();
		}
	}

	@Override
	public Optional<Variant> variant(ContentNode node, String variantId) {
		Objects.requireNonNull(variantId, "variantId");
		return variants(node).stream()
				.filter(variant -> variant.id().equals(variantId))
				.findFirst();
	}

	@Override
	public VariantContext variantContext(ContentNode node) {
		Objects.requireNonNull(node, "node");
		var location = variantLocation(node);
		var canonical = location
				.flatMap(value -> get(value.canonicalPath()))
				.orElse(node);
		var activeVariantId = location
				.filter(value -> !canonical.equals(node))
				.map(VariantLocation::variantId);
		return new VariantContext(canonical, activeVariantId, variants(canonical));
	}

	private ContentNode canonicalNode(ContentNode node) {
		return variantLocation(node)
				.flatMap(location -> get(location.canonicalPath()))
				.orElse(node);
	}

	private Optional<VariantLocation> variantLocation(ContentNode node) {
		var pathParts = normalize(node.path()).split("/");
		for (int index = 0; index < pathParts.length; index++) {
			if (!".variants".equals(pathParts[index])) {
				continue;
			}
			if (index + 3 != pathParts.length - 1) {
				return Optional.empty();
			}

			var pageFolder = pathParts[index + 1];
			var variantId = pathParts[index + 2];
			var fileName = pathParts[index + 3];
			if (!pageFolder.equals(removeMarkdown(fileName))) {
				return Optional.empty();
			}

			var canonicalPath = index == 0
					? fileName
					: String.join("/", java.util.Arrays.copyOf(pathParts, index)) + "/" + fileName;
			return Optional.of(new VariantLocation(canonicalPath, variantId));
		}
		return Optional.empty();
	}

	@Override
	public ContentQuery<ContentNode> query() {
		return content.query((node, excerptLength) -> node);
	}

	@Override
	public ContentQuery<ContentNode> query(String startPath) {
		return content.query(startPath, (node, excerptLength) -> node);
	}

	@Override
	public <T> ContentQuery<T> query(BiFunction<ContentNode, Integer, T> nodeMapper) {
		return content.query(nodeMapper);
	}

	@Override
	public <T> ContentQuery<T> query(
			String startPath,
			BiFunction<ContentNode, Integer, T> nodeMapper) {
		return content.query(startPath, nodeMapper);
	}

	private static String normalize(String path) {
		var normalized = path.replace('\\', '/');
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}

	private static String parentPath(String path) {
		var normalized = normalize(path);
		var separator = normalized.lastIndexOf('/');
		return separator < 0 ? "" : normalized.substring(0, separator);
	}

	private static String join(String parent, String child) {
		return parent.isEmpty() ? child : parent + "/" + child;
	}

	private static String fileName(String path) {
		var normalized = normalize(path);
		var separator = normalized.lastIndexOf('/');
		return separator < 0 ? normalized : normalized.substring(separator + 1);
	}

	private static String removeMarkdown(String filename) {
		return filename.endsWith(".md")
				? filename.substring(0, filename.length() - 3)
				: filename;
	}

	private record VariantCandidate(String id, String path) {
	}

	private record VariantLocation(String canonicalPath, String variantId) {
	}
}
