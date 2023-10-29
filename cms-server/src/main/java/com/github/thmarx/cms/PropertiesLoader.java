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

import com.github.thmarx.cms.api.HostProperties;
import com.github.thmarx.cms.api.ServerProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public abstract class PropertiesLoader {
	
	public static HostProperties hostProperties (Path path) throws IOException {
		Map<String, Object> properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		return new HostProperties(properties);
	}
	
	public static ServerProperties serverProperties (Path path) throws IOException {
		Map<String, Object> properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		return new ServerProperties(properties);
	}
}
