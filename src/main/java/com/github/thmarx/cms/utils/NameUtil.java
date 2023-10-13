package com.github.thmarx.cms.utils;

import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NameUtil {

	private static Object menuTitleExpression;
	static {
		try {
			menuTitleExpression = Ognl.parseExpression("data['menu']!=null ? data.menu['title'] : null");
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
			/*if (node.data().containsKey("menu") && node.data().get("menu") instanceof Map) {
				Map<String, Object> menu = (Map<String, Object>) node.data().get("menu");
				if (menu.containsKey("title")) {
					return (String) menu.get("title");
				}
			}*/
			if (node.data().containsKey("title")) {
				return (String) node.data().get("title");
			}
			
			
		} catch (OgnlException ex) {
			ex.printStackTrace();
			log.error("error getting name", ex);
		}
		return node.name();
	}
}
