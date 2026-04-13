package com.condation.cms.api.ui.elements;

/*-
 * #%L
 * CMS Api
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 *
 * @author thmar
 */
public class MediaForms {

	public Map<String, MetaForm> metaForms = new HashMap<>();


	public void registerForm(String name, Map<String, Object> metaForm) {
		metaForms.put(name, new MetaForm(name, metaForm));
	}

	
	
	public Map<String, MetaForm> getMetaForms () {
		return new HashMap<>(metaForms);
	}

	public static record MetaForm(String name, Map<String, Object> form) {	
	}
}
