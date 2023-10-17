/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms;

/*-
 * #%L
 * cms-server
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class HostProperties {
	
	private Properties properties;
	
	public HostProperties load (Path path) throws IOException {
		if (this.properties != null) {
			return this;
		}
		this.properties = new Properties();
		try (var reader = Files.newBufferedReader(path)) {
			properties.load(reader);
		}
		return this;
	}
	
	public String hostname () {
		return properties.getProperty("hostname");
	}
	
	public String templateEngine () {
		return properties.getProperty("template.engine", "freemarker");
	}
	public String markdownEngine () {
		return properties.getProperty("markdown.engine", "flexmark");
	}
}
