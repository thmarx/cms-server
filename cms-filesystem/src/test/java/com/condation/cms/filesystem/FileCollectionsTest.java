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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.api.workflow.WFStatusQueryProvider;
import com.condation.cms.api.workflow.WorkflowInstance;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

class FileCollectionsTest {

	@TempDir
	Path tempDirectory;

	@Test
	void queriesCollectionsWithFilteringSortingPagingAndRawMarkdown() throws Exception {
		write("blog/first.md", "title: First\nfeatured: true\nrank: 2", "# First");
		write("blog/second.md", "title: Second\nfeatured: false\nrank: 3", "**Second**");
		write("blog/third.md", "title: Third\nfeatured: true\nrank: 1", "_Third_");
		write("authors/first.md", "title: Author\nfeatured: true\nrank: 0", "Author body");
		write("blog/nested/ignored.md", "title: Ignored", "Ignored body");

		var collections = createCollections();
		try {
			Assertions.assertThat(collections.names()).containsExactlyInAnyOrder("blog", "authors");

			var page = collections.collection("blog")
					.query()
					.where("featured", true)
					.orderby("rank")
					.asc()
					.page(1, 1);

			Assertions.assertThat(page.getTotalItems()).isEqualTo(2);
			Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
			Assertions.assertThat(page.getItems())
					.singleElement()
					.satisfies(item -> {
						Assertions.assertThat(item.id()).isEqualTo("third");
						Assertions.assertThat(item.collection()).isEqualTo("blog");
						Assertions.assertThat(item.path()).isEqualTo("blog/third.md");
						Assertions.assertThat(item.content()).isEqualTo("_Third_\r\n");
						Assertions.assertThat(item.meta()).containsEntry("title", "Third");
					});

			Assertions.assertThat(collections.collection("authors").query().get())
					.extracting(item -> item.id())
					.containsExactly("first");
		} finally {
			collections.close();
		}
	}

	@Test
	void appliesFlatFileChangesIncrementally() throws Exception {
		var item = write("blog/item.md", "title: Before\nfeatured: false", "Before");
		var collections = createCollections();
		try {
			write("blog/item.md", "title: After\nfeatured: true", "After");
			collections.handleEvent(new FileEvent(item.toFile(), FileEvent.Type.MODIFIED));
			collections.flushChanges();

			Assertions.assertThat(collections.collection("blog").query().where("featured", true).get())
					.singleElement()
					.satisfies(result -> {
						Assertions.assertThat(result.meta()).containsEntry("title", "After");
						Assertions.assertThat(result.content()).isEqualTo("After\r\n");
					});

			Files.delete(item);
			collections.handleEvent(new FileEvent(item.toFile(), FileEvent.Type.DELETED));
			collections.flushChanges();
			Assertions.assertThat(collections.collection("blog").query().get()).isEmpty();
		} finally {
			collections.close();
		}
	}

	@Test
	void rejectsUnsafeCollectionNames() throws Exception {
		var collections = createCollections();
		try {
			Assertions.assertThatIllegalArgumentException()
					.isThrownBy(() -> collections.collection("../content"));
		} finally {
			collections.close();
		}
	}

	@Test
	void appliesDefaultWorkflowSchedulingAndPreview() throws Exception {
		writeMeta("blog/published.md", "status: published", "Published");
		writeMeta("blog/draft.md", "status: draft", "Draft");
		writeMeta(
				"blog/future.md",
				"status: published\npublish_date: 2099-01-01T00:00:00Z",
				"Future");
		writeMeta(
				"blog/expired.md",
				"status: published\nunpublish_date: 2000-01-01T00:00:00Z",
				"Expired");

		var collections = createCollections();
		try {
			var publicPage = collections.collection("blog").query().page(1, 10);
			Assertions.assertThat(publicPage.getTotalItems()).isEqualTo(1);
			Assertions.assertThat(publicPage.getItems())
					.extracting(item -> item.id())
					.containsExactly("published");

			var previewContext = new RequestContext();
			previewContext.add(IsPreviewFeature.class, new IsPreviewFeature(IsPreviewFeature.Mode.PREVIEW));
			var previewItems = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, previewContext)
					.call(() -> collections.collection("blog").query().get());

			Assertions.assertThat(previewItems)
					.extracting(item -> item.id())
					.containsExactlyInAnyOrder("published", "draft", "future", "expired");
		} finally {
			collections.close();
		}
	}

	@Test
	void appliesConfiguredContentWorkflow() throws Exception {
		writeMeta("blog/approved.md", "status: draft\napproval: approved", "Approved");
		writeMeta("blog/blocked.md", "status: published\napproval: blocked", "Blocked");

		var collections = createCollections();
		try {
			var requestContext = new RequestContext();
			requestContext.add(
					WorkflowFeature.class,
					new WorkflowFeature(new WorkflowInstance("approval", "Approval", approvalStatusProvider())));

			var result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext)
					.call(() -> collections.collection("blog").query().page(1, 10));

			Assertions.assertThat(result.getTotalItems()).isEqualTo(1);
			Assertions.assertThat(result.getItems())
					.singleElement()
					.satisfies(item -> Assertions.assertThat(item.id()).isEqualTo("approved"));
		} finally {
			collections.close();
		}
	}

	private FileCollections createCollections() throws Exception {
		var collections = new FileCollections("test-site", tempDirectory, FileCollectionsTest::parseMeta);
		collections.init();
		return collections;
	}

	private Path write(String relativePath, String meta, String body) throws Exception {
		return writeMeta(relativePath, "status: published\n" + meta, body);
	}

	private Path writeMeta(String relativePath, String meta, String body) throws Exception {
		var file = tempDirectory.resolve("collections").resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.writeString(file, "---\n%s\n---\n%s\n".formatted(meta, body));
		return file;
	}

	private static WFStatusProvider approvalStatusProvider() {
		return new WFStatusQueryProvider() {
			@Override
			public boolean isPublished(ContentNode node) {
				return "approved".equals(node.data().get("approval"));
			}

			@Override
			public Status status(ContentNode node) {
				return new Status(
						isPublished(node),
						null,
						null,
						true,
						(String) node.data().get("approval"));
			}

			@Override
			public String newNodeStatus() {
				return "blocked";
			}

			@Override
			public <T> Optional<ContentQuery<T>> published(ContentQuery<T> query) {
				return Optional.of(query.where("approval", "approved"));
			}

			@Override
			public <T> ContentQuery<T> unpublished(ContentQuery<T> query) {
				return query.where("approval", "!=", "approved");
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseMeta(Path file) {
		try {
			var content = Files.readString(file);
			var parts = content.split("---", 3);
			return parts.length == 3 ? new Yaml().load(parts[1]) : Map.of();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
