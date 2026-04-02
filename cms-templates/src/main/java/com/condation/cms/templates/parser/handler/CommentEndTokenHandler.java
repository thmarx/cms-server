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

import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.lexer.Token;
import com.condation.cms.templates.parser.ParserContext;

/**
 * Handles COMMENT_END tokens by popping the current node from the stack.
 */
public class CommentEndTokenHandler implements TokenHandler {

	@Override
	public void handle(Token token, ParserContext context) {
		if (context.hasNodes()) {
			context.popNode();
		} else {
			throw new ParserException("Unexpected token: COMMENT_END", token.line, token.column);
		}
	}
}
