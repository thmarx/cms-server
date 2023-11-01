/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * example-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.extensions.TemplateModelExtendingExtentionPoint;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.modules.api.annotation.Extension;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author thmar
 */
@Extension(TemplateModelExtendingExtentionPoint.class)
public class ExampleTemplateModelExtendingExtensionEndPoint extends TemplateModelExtendingExtentionPoint {

	@Override
	public void extendModel(TemplateEngine.Model model) {
		model.values.put("searcher", new Searcher());
	}

	@Override
	public void init() {
	}
	
	public static class Searcher {
		public Result search (String query) {
			Result result = new Result(List.of(
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString()),
					new Item(UUID.randomUUID().toString())
			), 100);
			
			return result;
		}
	}
	
	public static record Result (List<Item> items, int total){};
	
	public static record Item (String name){};
}
