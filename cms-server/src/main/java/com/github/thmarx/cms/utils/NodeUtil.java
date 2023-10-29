package com.github.thmarx.cms.utils;

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
import com.github.thmarx.cms.Constants;
import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NodeUtil {

	public static String getName(MetaData.MetaNode node) {

		Map<String, Object> menu = (Map<String, Object>) node.data().getOrDefault("menu", Collections.EMPTY_MAP);

		if (menu.containsKey("title")) {
			return (String) menu.get("title");
		}
		if (node.data().containsKey("title")) {
			return (String) node.data().get("title");
		}

		return node.name();
	}

	public static Double getMenuPosition(MetaData.MetaNode node) {

		Map<String, Object> menu = (Map<String, Object>) node.data().getOrDefault("menu", Collections.EMPTY_MAP);

		if (menu.containsKey("position")) {
			var number = (Number) menu.get("position");
			return number.doubleValue();
		}

		return Constants.DEFAULT_MENU_POSITION;
	}
}