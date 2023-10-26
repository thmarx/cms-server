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
import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import ognl.OgnlException;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NodeUtil {

	private static Object menuTitleExpression;
	private static Object menuPositionExpression;
	static {
		try {
			menuTitleExpression = Ognl.parseExpression("data['menu']!=null ? data.menu['title'] : null");
			menuPositionExpression = Ognl.parseExpression("data['menu']!=null ? data.menu['position'] : null");
		} catch (OgnlException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String getName(MetaData.MetaNode node) {
		try {
			var context = Ognl.createDefaultContext(node);
			String menuTitle = (String) Ognl.getValue(menuTitleExpression, context, node, String.class);
			if (menuTitle != null) {
				return menuTitle;
			}
			if (node.data().containsKey("title")) {
				return (String) node.data().get("title");
			}
			
			
		} catch (OgnlException ex) {
			log.error("error getting name", ex);
		}
		return node.name();
	}
	
	public static float getMenuPosition (MetaData.MetaNode node) {
		try {
			var context = Ognl.createDefaultContext(node);
			Float menuPosition = (Float) Ognl.getValue(menuPositionExpression, context, node, Float.class);
			if (menuPosition != null) {
				return menuPosition;
			}
		} catch (OgnlException ex) {
			log.error("error getting menu position", ex);
		}
		return Constants.DEFAULT_MENU_POSITION;
	}
}
