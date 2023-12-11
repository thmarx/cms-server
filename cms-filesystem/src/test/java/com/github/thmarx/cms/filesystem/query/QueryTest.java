package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.filesystem.index.IndexProviding;
import com.github.thmarx.cms.filesystem.index.SecondaryIndex;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class QueryTest {

	private static Collection<ContentNode> nodes;

	private IndexProviding indexProviding = new IndexProviding() {
		private ConcurrentMap<String, SecondaryIndex<?>> secondaryIndexes = new ConcurrentHashMap<>();

		@Override
		public SecondaryIndex<?> getOrCreateIndex(String field, Function<ContentNode, Object> indexFunction) {
			if (!secondaryIndexes.containsKey(field)) {
				var index = SecondaryIndex.<Object>builder()
						.indexFunction(indexFunction)
						.build();
				index.addAll(nodes);
				secondaryIndexes.put(field, index);
			}

			return secondaryIndexes.get(field);
		}
	};

	@BeforeAll
	public static void setup() {
		nodes = new ArrayList<>();
		ContentNode node = new ContentNode("/", "index.md", Map.of(
				"featured", true,
				Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));
		nodes.add(node);
		node = new ContentNode("/2", "index2.md", Map.of(
				"featured", true,
				Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now().minus(1, ChronoUnit.DAYS))));
		nodes.add(node);
		node = new ContentNode("/test1", "test1.md", Map.of(
				"featured", false,
				"index", 1, 
				Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
				"tags", List.of("three", "four")
		));
		nodes.add(node);
		node = new ContentNode("/test2", "test2.md", Map.of(
				"featured", false,
				"index", 2,
				Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
				"tags", List.of("one", "two"))
		);
		nodes.add(node);
		
		node = new ContentNode("/json", "test-json.md", Map.of(
				"featured", false,
				"index", 2,
				Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
				"tags", List.of("one", "two"),
				"content", Map.of("type", Constants.ContentTypes.JSON))
		);
		nodes.add(node);
	}

	protected Query<ContentNode> createQuery() {
		return new Query<>(nodes, indexProviding, (node, i) -> node);
	}
	
	
	@Test
	public void test_eq() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", true).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("/2");

		query = createQuery();
		nodes = query.where("featured", "=", true).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("/2");
	}

	@Test
	public void test_not() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", "!=", true).get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.stream().map(ContentNode::uri).toList()).contains("/test1", "/test2");
	}

	@Test
	public void test_data() {
		Query<ContentNode> query = createQuery();
		var nodes = query.get();
		Assertions.assertThat(nodes).hasSize(3);
	}

	@Test
	public void test_sort_asc() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", false).orderby("index").asc().get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
		Assertions.assertThat(nodes.get(1).uri()).isEqualTo("/test2");
	}

	@Test
	public void test_sort_desc() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", false).orderby("index").desc().get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
		Assertions.assertThat(nodes.get(1).uri()).isEqualTo("/test1");
	}

	@Test
	public void test_offset_0() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", false).orderby("index").desc().get(0, 1);
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}

	@Test
	public void test_offset_1() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("featured", false).orderby("index").desc().get(1, 1);
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}

	@Test
	public void test_contains() {
		Query<ContentNode> query = createQuery();
		var nodes = query.whereContains("tags", "one").get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}

	@Test
	public void test_contains_not() {
		Query<ContentNode> query = createQuery();
		var nodes = query.whereNotContains("tags", "one").get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}

	@Test
	public void test_gt() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("index", ">", 1).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}

	@Test
	public void test_gte() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("index", ">=", 2).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}

	@Test
	public void test_lt() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("index", "<", 2).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}

	@Test
	public void test_lte() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("index", "<=", 1).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}

	@Test
	public void test_group_by() {
		Query<ContentNode> query = createQuery();
		var nodes = query.groupby("featured");
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes).containsKeys(true, false);
		Assertions.assertThat(nodes.get(true).stream().map(ContentNode::uri).toList()).contains("/", "/2");
		Assertions.assertThat(nodes.get(false).stream().map(ContentNode::uri).toList()).contains("/test1", "/test2");
	}

	@Test
	public void test_in() {
		Query<ContentNode> query = createQuery();
		var nodes = query.whereIn("index", 1, 2).get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.stream().map(ContentNode::uri).toList()).contains("/test1", "/test2");
	}

	@Test
	public void test_not_in() {
		Query<ContentNode> query = createQuery();
		var nodes = query.whereIn("index", 1, 3, 4).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.stream().map(ContentNode::uri).toList()).contains("/test1");
	}
	
	@Test
	public void test_json() {
		Query<ContentNode> query = createQuery();
		var nodes = query.json().get(0, 1);
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/json");
	}
	
	@Test
	public void test_where_not_null() {
		Query<ContentNode> query = createQuery();
		var nodes = query.where("index", "!=", null).get();
		Assertions.assertThat(nodes).hasSize(2);
		
		query = createQuery();
		query.where("index", "=", null).get();
		Assertions.assertThat(nodes).hasSize(2);
	}
}
