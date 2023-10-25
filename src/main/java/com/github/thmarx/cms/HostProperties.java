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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class HostProperties {
	
	private Map<String, Object> properties;
	
	public HostProperties load (Path path) throws IOException {
		if (this.properties != null) {
			return this;
		}
		
		this.properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		
		return this;
	}
	
	public String hostname () {
		return (String) properties.getOrDefault("hostname", "localhost");
	}
	
	private Map<String, Object> getSubMap (final String name) {
		return (Map<String, Object>) properties.getOrDefault(name, Collections.emptyMap());
	}
	
	public String templateEngine () {
		return (String)getSubMap("template").getOrDefault("engine", "freemarker");
	}
	public String markdownEngine () {
		return (String)getSubMap("markdown").getOrDefault("engine", "flexmark");
	}
}
