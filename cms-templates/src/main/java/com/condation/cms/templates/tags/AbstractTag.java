package com.condation.cms.templates.tags;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import com.condation.cms.templates.renderer.ScopeContext;
import org.apache.commons.jexl3.JexlExpression;

/**
 *
 * @author t.marx
 */
public class AbstractTag {
	
	protected Object evaluateExpression (TagNode node, JexlExpression expression, Renderer.Context context, ScopeContext scopeContext) {
		try {
			return expression.evaluate(scopeContext);
		} catch (Exception e) {
			throw new RenderException(e.getLocalizedMessage(), node.getLine(), node.getColumn());
		}
	}
}
