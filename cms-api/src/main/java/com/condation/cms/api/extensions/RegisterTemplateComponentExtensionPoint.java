package com.condation.cms.api.extensions;

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


import com.condation.cms.api.model.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author t.marx
 */
public abstract class RegisterTemplateComponentExtensionPoint extends AbstractExtensionPoint {
	
	public Map<String, Function<Parameter, String>> components () {
		return Collections.emptyMap();
	}
	
	public List<Object> componentDefinitions () {
		return Collections.emptyList();
	}
}
