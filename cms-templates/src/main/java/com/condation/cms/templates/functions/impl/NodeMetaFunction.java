package com.condation.cms.templates.functions.impl;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.ContentRenderer;
import com.condation.cms.content.Section;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class NodeMetaFunction extends AbstractNodeFunction {

	public static final String NAME = "select_node_meta";

	public NodeMetaFunction(RequestContext requestContext) {
		super(requestContext);
	}

	@Override
	public String name() {
		return NAME;
	}
	
}
