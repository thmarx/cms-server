package com.condation.cms.content.template.functions;

/*-
 * #%L
 * CMS Content
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
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.repository.ContentRepository;
import com.condation.cms.api.utils.PathUtil;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public abstract class AbstractCurrentNodeFunction {

	protected final DB db;
	protected final ReadOnlyFile currentNode;
	protected final ContentRepository contentRepository;
	protected final ContentNodeMapper contentNodeMapper;
	protected final RequestContext context;

	protected boolean isPreview() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return true;
		}

		return false;
	}
	protected String getPreviewMode() {
		if (RequestContextScope.REQUEST_CONTEXT.isBound()
				&& RequestContextScope.REQUEST_CONTEXT.get().has(IsPreviewFeature.class)) {
			return RequestContextScope.REQUEST_CONTEXT.get().get(IsPreviewFeature.class).mode().getValue();
		}

		return IsPreviewFeature.Mode.PREVIEW.getValue();
	}

	protected String repositoryPath(ReadOnlyFile base, String path) {
		var basePath = PathUtil.toRelativePath(base, db.getFileSystem().contentBase())
				.replace('\\', '/');
		var childPath = path.replace('\\', '/');
		while (childPath.startsWith("./")) {
			childPath = childPath.substring(2);
		}
		while (childPath.startsWith("/")) {
			childPath = childPath.substring(1);
		}
		if (childPath.isEmpty()) {
			return basePath;
		}
		return basePath.isEmpty() ? childPath : basePath + "/" + childPath;
	}
}
