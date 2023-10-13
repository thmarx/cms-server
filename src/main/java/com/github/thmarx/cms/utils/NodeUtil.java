package com.github.thmarx.cms.utils;

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
	private static Object menuOrderExpression;
	static {
		try {
			menuTitleExpression = Ognl.parseExpression("data['menu']!=null ? data.menu['title'] : null");
			menuOrderExpression = Ognl.parseExpression("data['menu']!=null ? data.menu['order'] : null");
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
	
	public static float getMenuOrder (MetaData.MetaNode node) {
		try {
			var context = Ognl.createDefaultContext(node);
			Float menuOrder = (Float) Ognl.getValue(menuOrderExpression, context, node, Float.class);
			if (menuOrder != null) {
				return menuOrder;
			}
		} catch (OgnlException ex) {
			log.error("error getting menu order", ex);
		}
		return Constants.DEFAULT_MENU_ORDER;
	}
}
