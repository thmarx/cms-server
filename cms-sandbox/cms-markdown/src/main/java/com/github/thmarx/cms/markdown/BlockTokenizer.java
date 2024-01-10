package com.github.thmarx.cms.markdown;

/*-
 * #%L
 * cms-markdown
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class BlockTokenizer {

    
	protected List<Block> tokenize (final String original_md) throws IOException {
		
		var md = original_md.replaceAll("\r\n", "\n");
		
		List<Block> blocks = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new StringReader(md))) {
			String line = null;
			StringBuilder blockContent = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if ("".equals(line)) {
					blocks.add(new Block(blockContent.toString()));
					blockContent.setLength(0);
					
					continue;
				}
				blockContent.append(line).append("\n");
			}
			if (!blockContent.isEmpty()) {
				blocks.add(new Block(blockContent.toString()));
			}
		}
		return blocks;
	}
}
