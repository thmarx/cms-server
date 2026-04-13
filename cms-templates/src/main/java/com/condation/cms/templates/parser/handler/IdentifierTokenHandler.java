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

import com.condation.cms.templates.exceptions.UnknownTagException;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.parser.ParserContext;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.VariableNode;
import com.condation.cms.templates.utils.TemplateUtils;

/**
 * Handles IDENTIFIER tokens by setting names and expressions on the current node.
 * Different behavior depending on the current node type (Tag, Variable, or Component).
 */
public class IdentifierTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (!context.hasNodes()) {
			return;
		}

		ASTNode currentNode = context.currentNode();

		if (currentNode instanceof TagNode tagNode) {
			handleTagIdentifier(token, context, tagNode);
		} else if (currentNode instanceof VariableNode variableNode) {
			handleVariableIdentifier(token, context, variableNode);
		} else if (currentNode instanceof ComponentNode componentNode) {
			componentNode.setName(token.value);
		}
	}

	private void handleTagIdentifier(Token token, ParserContext context, TagNode tagNode) {
		String tagName = token.value != null ? token.value.trim() : token.value;

		if (!context.getConfiguration().hasTag(tagName)) {
			throw new UnknownTagException(
					"Unknown tag (%s)".formatted(tagName),
					tagNode.getLine(),
					tagNode.getColumn()
			);
		}

		tagNode.setName(tagName);
	}

	private void handleVariableIdentifier(Token token, ParserContext context, VariableNode variableNode) {
		String identifier = token.value;

		if (identifier != null && TemplateUtils.hasFilters(identifier)) {
			String variable = TemplateUtils.extractVariableName(identifier);

			variableNode.setVariable(variable);
			variableNode.setExpression(context.getEngine().createExpression(variable));

			variableNode.setFilters(
					TemplateUtils.extractFilters(identifier)
							.stream()
							.map(TemplateUtils::parseFilter)
							.toList()
			);
		} else {
			variableNode.setVariable(token.value);
			if (token.value != null) {
				variableNode.setExpression(context.getEngine().createExpression(token.value));
			}
		}
	}
}
