package com.condation.cms.templates.parser.handler;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ParserContext;
import com.condation.cms.templates.parser.VariableNode;

/**
 * Handles VARIABLE_START tokens by creating and pushing a VariableNode.
 */
public class VariableStartTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (context.hasNodes()) {
			VariableNode variableNode = new VariableNode(token.line, token.column);
			context.addChildToCurrentNode(variableNode);
			context.pushNode(variableNode);
		}
	}
}
