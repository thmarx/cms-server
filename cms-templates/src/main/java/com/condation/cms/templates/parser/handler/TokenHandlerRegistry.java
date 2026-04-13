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

import com.condation.cms.templates.lexer.Token;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry that maps token types to their corresponding handlers.
 * Uses EnumMap for optimal performance.
 */
public class TokenHandlerRegistry {

	private final Map<Token.Type, TokenHandler> handlers = new EnumMap<>(Token.Type.class);

	/**
	 * Registers a handler for a specific token type.
	 *
	 * @param type    the token type
	 * @param handler the handler for this token type
	 * @return this registry for fluent chaining
	 */
	public TokenHandlerRegistry register(Token.Type type, TokenHandler handler) {
		handlers.put(type, handler);
		return this;
	}

	/**
	 * Gets the handler for a specific token type.
	 *
	 * @param type the token type
	 * @return the handler, or null if not registered
	 */
	public TokenHandler getHandler(Token.Type type) {
		return handlers.get(type);
	}

	/**
	 * Checks if a handler is registered for a specific token type.
	 *
	 * @param type the token type
	 * @return true if a handler is registered
	 */
	public boolean hasHandler(Token.Type type) {
		return handlers.containsKey(type);
	}

	/**
	 * Creates and returns a registry with all default handlers registered.
	 *
	 * @return a fully configured registry
	 */
	public static TokenHandlerRegistry createDefault() {
		TokenHandlerRegistry registry = new TokenHandlerRegistry();

		registry.register(Token.Type.TEXT, new TextTokenHandler());
		registry.register(Token.Type.COMMENT_VALUE, new CommentValueTokenHandler());
		registry.register(Token.Type.VARIABLE_START, new VariableStartTokenHandler());
		registry.register(Token.Type.VARIABLE_END, new VariableEndTokenHandler());
		registry.register(Token.Type.COMMENT_START, new CommentStartTokenHandler());
		registry.register(Token.Type.COMMENT_END, new CommentEndTokenHandler());
		registry.register(Token.Type.TAG_START, new TagStartTokenHandler());
		registry.register(Token.Type.TAG_END, new TagEndTokenHandler());
		registry.register(Token.Type.COMPONENT_START, new ComponentStartTokenHandler());
		registry.register(Token.Type.COMPONENT_END, new ComponentEndTokenHandler());
		registry.register(Token.Type.IDENTIFIER, new IdentifierTokenHandler());
		registry.register(Token.Type.EXPRESSION, new ExpressionTokenHandler());
		registry.register(Token.Type.END, new EndTokenHandler());

		return registry;
	}
}
