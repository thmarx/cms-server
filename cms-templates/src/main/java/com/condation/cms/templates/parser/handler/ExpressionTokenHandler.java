package com.condation.cms.templates.parser.handler;

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

import com.condation.cms.templates.Tag;
import com.condation.cms.templates.exceptions.UnknownTagException;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.parser.ParserContext;
import com.condation.cms.templates.parser.TagNode;

/**
 * Handles EXPRESSION tokens by setting expressions on Tag or Component nodes.
 */
public class ExpressionTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (!context.hasNodes()) {
			return;
		}

		ASTNode currentNode = context.currentNode();

		if (currentNode instanceof TagNode tagNode) {
			handleTagExpression(token, context, tagNode);
		} else if (currentNode instanceof ComponentNode componentNode) {
			componentNode.setParameters(token.value);
		}
	}

	private void handleTagExpression(Token token, ParserContext context, TagNode tagNode) {
		tagNode.setCondition(token.value);

		if (!context.getConfiguration().hasTag(tagNode.getName())) {
			throw new UnknownTagException(
					"Unknown tag (%s)".formatted(tagNode.getName()),
					tagNode.getLine(),
					tagNode.getColumn()
			);
		}

		Tag tag = context.getConfiguration().getTag(tagNode.getName()).get();

		if (tag.parseExpressions() && token.value != null) {
			tagNode.setExpression(context.getEngine().createExpression(token.value));
		}
	}
}
