package com.condation.cms.api.module;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.feature.FeatureContainer;
import com.condation.cms.api.request.RequestContext;
import com.condation.modules.api.ModuleRequestContext;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class CMSRequestContext extends RequestContext implements ModuleRequestContext {
	
	private final RequestContext delegate;

	@Override
	public void close() throws Exception {
		if (delegate != null) {
			delegate.close();
		}
	}

	@Override
	public <T extends Feature> T get(Class<T> featureClass) {
		if (delegate == null) {
			return null;
		}
		return delegate.get(featureClass);
	}

	@Override
	public <T extends Feature> void add(Class<T> featureClass, T feature) {
		if (delegate == null) {
			return;
		}
		delegate.add(featureClass, feature);
	}

	@Override
	public boolean has(Class<? extends Feature> featureClass) {
		if (delegate == null) {
			return false;
		}
		return delegate.has(featureClass);
	}
	
	
}
