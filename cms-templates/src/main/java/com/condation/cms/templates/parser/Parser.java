package com.condation.cms.templates.parser;

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

import com.condation.cms.templates.lexer.TokenStream;
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.parser.handler.TokenHandler;
import com.condation.cms.templates.parser.handler.TokenHandlerRegistry;
import java.util.Stack;

import com.condation.cms.templates.lexer.Token;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;

/**
 * Parser that converts a token stream into an Abstract Syntax Tree (AST).
 * Uses the Strategy pattern with TokenHandlers for processing different token types.
 */
@RequiredArgsConstructor
public class Parser {

	private final TemplateConfiguration configuration;

	private final JexlEngine engine;

	private final TokenHandlerRegistry handlerRegistry = TokenHandlerRegistry.createDefault();

	public ASTNode parse(final TokenStream tokenStream) {
		return _parse(tokenStream, new ParserConfiguration(configuration));
	}

	private ASTNode _parse(final TokenStream tokenStream, final ParserConfiguration parserConfiguration) {
		ASTNode root = new ASTNode(0, 0);
		Stack<ASTNode> nodeStack = new Stack<>();
		nodeStack.push(root);

		ParserContext context = new ParserContext(nodeStack, parserConfiguration, engine);

		Token token;
		while ((token = tokenStream.peek()) != null) {
			TokenHandler handler = handlerRegistry.getHandler(token.type);

			if (handler != null) {
				handler.handle(token, context);
			} else {
				throw new ParserException("Unexpected token: " + token.type, token.line, token.column);
			}

			tokenStream.next();
		}

		if (nodeStack.size() > 1) {
			throw new ParserException("Unclosed tag or block detected", 0, 0);
		}

		return root;
	}
}
