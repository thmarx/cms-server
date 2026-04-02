package com.condation.cms.templates.parser;

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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;

import java.util.Stack;

/**
 * Encapsulates the parsing context including the node stack and configuration.
 * This class provides shared state for all token handlers.
 */
@RequiredArgsConstructor
public class ParserContext {

	@Getter
	private final Stack<ASTNode> nodeStack;

	@Getter
	private final ParserConfiguration configuration;

	@Getter
	private final JexlEngine engine;

	/**
	 * Checks if the node stack is empty.
	 */
	public boolean hasNodes() {
		return !nodeStack.isEmpty();
	}

	/**
	 * Returns the current node on top of the stack without removing it.
	 */
	public ASTNode currentNode() {
		return nodeStack.peek();
	}

	/**
	 * Pushes a node onto the stack.
	 */
	public void pushNode(ASTNode node) {
		nodeStack.push(node);
	}

	/**
	 * Pops a node from the stack.
	 */
	public ASTNode popNode() {
		return nodeStack.pop();
	}

	/**
	 * Adds a child to the current node on top of the stack.
	 */
	public void addChildToCurrentNode(ASTNode child) {
		if (hasNodes()) {
			currentNode().addChild(child);
		}
	}
}
