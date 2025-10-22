package com.condation.cms.filesystem.virtual;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.api.Constants;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author thmar
 */
@RequiredArgsConstructor
public class TemplateFileSystemProvider implements VirtualFileSystemProvider {

	private final String scheme;
	
	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public Path resolve(String path, SiteContext site) {
		var file = site.db().getFileSystem().resolve(Constants.Folders.TEMPLATES).resolve(path);
		if (!Files.exists(file) && site.theme() != null) {
			file = site.theme().templatesPath().resolve(path);
			if (!Files.exists(file) && site.theme().getParentTheme() != null) {
				file = site.theme().getParentTheme().templatesPath().resolve(path);
			}
		}
		return file;
	}
	
}
