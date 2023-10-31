package com.github.thmarx.cms.template.functions.navigation;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.ContentParser;
import com.github.thmarx.cms.TestHelper;
import com.github.thmarx.cms.eventbus.EventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author thmar
 */
public class NavigationFunctionNGTest {

	NavigationFunction navigationFunction;

	@BeforeClass
	void init() throws IOException {
		FileSystem fileSystem = new FileSystem(Path.of("hosts/test"), new EventBus());
		fileSystem.init();
		var markdownRenderer = TestHelper.getRenderer();
		navigationFunction = new NavigationFunction(fileSystem, Path.of("hosts/test/content/nav/index.md"), new ContentParser(fileSystem),
				markdownRenderer);
	}

	@Test
	public void test_root() {

		List<NavNode> list = navigationFunction.list("/nav");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/nav", "/nav/folder1");
	}

	@Test
	public void test_folder1() {

		List<NavNode> list = navigationFunction.list("/nav/folder1");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/nav/folder1/test", "/nav/folder1");
	}

	@Test
	public void test_draft() {

		List<NavNode> list = navigationFunction.list("/nav2");

		Assertions.assertThat(list).isEmpty();
	}
	
	@Test
	public void test_visibility() {

		List<NavNode> list = navigationFunction.list("/visibility");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/visibility/folder1");
	}
}
