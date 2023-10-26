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

import com.github.thmarx.cms.api.extensions.HttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.http.Request;
import com.github.thmarx.cms.api.extensions.http.Response;
import com.github.thmarx.modules.api.annotation.Extension;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author t.marx
 */
@Extension(HttpHandlerExtensionPoint.class)
public class ExampleHttpHandlerExtensionPoint extends HttpHandlerExtensionPoint {

    @Override
	public void execute(Request request, Response response) {
		response.write("hello from example module", StandardCharsets.UTF_8);
	}

	@Override
	public boolean handles(String method, String uri) {
		return "GET".equalsIgnoreCase(method) && uri.equalsIgnoreCase("test");
	}

	@Override
	public void init() {
		
	}
}
