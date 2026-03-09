package com.condation.cms.core.db.taxonomy;

/*-
 * #%L
 * cms-core
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

import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.TaxonomyStore;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.core.configuration.GSONProvider;
import com.condation.cms.core.configuration.source.TomlConfigSource;
import com.condation.cms.core.configuration.source.YamlConfigSource;
import io.github.wasabithumb.jtoml.JToml;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
public class FileTaxonomyStore implements TaxonomyStore {

	private final Path hostBase;
	private final Yaml yaml;
	private static final JToml JTOML = JToml.jToml();

	public FileTaxonomyStore(Path hostBase) {
		this.hostBase = hostBase;

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		this.yaml = new Yaml(options);
	}

	private Path getTaxonomyDefinitionFile() {
		var yamlFile = hostBase.resolve("config/taxonomy.yaml");
		if (Files.exists(yamlFile)) {
			return yamlFile;
		}
		var tomlFile = hostBase.resolve("config/taxonomy.toml");
		if (Files.exists(tomlFile)) {
			return tomlFile;
		}
		return yamlFile;
	}

	private Path getTaxonomyValuesFile(String slug) {
		var yamlFile = hostBase.resolve("config/taxonomy.%s.yaml".formatted(slug));
		if (Files.exists(yamlFile)) {
			return yamlFile;
		}
		var tomlFile = hostBase.resolve("config/taxonomy.%s.toml".formatted(slug));
		if (Files.exists(tomlFile)) {
			return tomlFile;
		}
		return yamlFile; // default to yaml if neither exists
	}

	@Override
	public List<Taxonomy> all() {
		try {
			Path file = getTaxonomyDefinitionFile();
			if (!Files.exists(file)) {
				return List.of();
			}

			List<Object> taxonomiesData;
			if (file.toString().endsWith(".toml")) {
				var source = TomlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			} else {
				var source = YamlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			}

			return taxonomiesData.stream()
					.map(item -> GSONProvider.GSON.fromJson(GSONProvider.GSON.toJson(item), Taxonomy.class))
					.peek(this::loadValues)
					.collect(Collectors.toList());
		} catch (IOException e) {
			log.error("Error reading taxonomies", e);
			return List.of();
		}
	}

	private void loadValues(Taxonomy taxonomy) {
		try {
			Path file = getTaxonomyValuesFile(taxonomy.getSlug());
			if (!Files.exists(file)) {
				taxonomy.setValues(new HashMap<>());
				return;
			}

			List<Object> valuesData;
			if (file.toString().endsWith(".toml")) {
				var source = TomlConfigSource.build(file);
				valuesData = source.getList("values");
			} else {
				var source = YamlConfigSource.build(file);
				valuesData = source.getList("values");
			}

			Map<String, Value> values = valuesData.stream()
					.map(item -> GSONProvider.GSON.fromJson(GSONProvider.GSON.toJson(item), Value.class))
					.collect(Collectors.toMap(Value::getId, v -> v));
			taxonomy.setValues(values);
		} catch (IOException e) {
			log.error("Error reading taxonomy values for " + taxonomy.getSlug(), e);
		}
	}

	@Override
	public Optional<Taxonomy> forSlug(String slug) {
		try {
			Path file = getTaxonomyDefinitionFile();
			if (!Files.exists(file)) {
				return Optional.empty();
			}
			List<Object> taxonomiesData;
			if (file.toString().endsWith(".toml")) {
				var source = TomlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			} else {
				var source = YamlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			}

			Optional<Taxonomy> taxoOpt = taxonomiesData.stream()
					.map(item -> GSONProvider.GSON.fromJson(GSONProvider.GSON.toJson(item), Taxonomy.class))
					.filter(t -> t.getSlug().equals(slug))
					.findFirst();

			taxoOpt.ifPresent(this::loadValues);
			return taxoOpt;
		} catch (IOException e) {
			log.error("Error reading taxonomy for slug " + slug, e);
			return Optional.empty();
		}
	}

	@Override
	public synchronized void saveTaxonomy(Taxonomy taxonomy) throws IOException {
		// To save definitions without values, we load definitions from the file directly
		Path file = getTaxonomyDefinitionFile();
		List<Taxonomy> allDefinitions = new ArrayList<>();
		if (Files.exists(file)) {
			List<Object> taxonomiesData;
			if (file.toString().endsWith(".toml")) {
				var source = TomlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			} else {
				var source = YamlConfigSource.build(file);
				taxonomiesData = source.getList("taxonomies");
			}
			allDefinitions = taxonomiesData.stream()
					.map(item -> GSONProvider.GSON.fromJson(GSONProvider.GSON.toJson(item), Taxonomy.class))
					.collect(Collectors.toList());
		}

		boolean found = false;
		for (int i = 0; i < allDefinitions.size(); i++) {
			if (allDefinitions.get(i).getSlug().equals(taxonomy.getSlug())) {
				allDefinitions.set(i, taxonomy);
				found = true;
				break;
			}
		}
		if (!found) {
			allDefinitions.add(taxonomy);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("taxonomies", allDefinitions.stream().map(this::toPlainMapWithoutValues).collect(Collectors.toList()));

		saveAtomic(file, data);
	}

	private Map<String, Object> toPlainMapWithoutValues(Taxonomy t) {
		Map<String, Object> m = toPlainMap(t);
		m.remove("values");
		return m;
	}

	private Map<String, Object> toPlainMap(Object o) {
		var json = GSONProvider.GSON.toJsonTree(o);
		return GSONProvider.GSON.fromJson(json, HashMap.class);
	}

	private Object unwrap(Object value) {
		if (value instanceof Map m) {
			Map<String, Object> unwrapped = new HashMap<>();
			for (Object key : m.keySet()) {
				unwrapped.put(key.toString(), unwrap(m.get(key)));
			}
			return unwrapped;
		} else if (value instanceof List l) {
			List<Object> unwrapped = new ArrayList<>();
			for (Object item : l) {
				unwrapped.add(unwrap(item));
			}
			return unwrapped;
		} else {
			return value;
		}
	}

	private void saveAtomic(Path file, Map<String, Object> data) throws IOException {
		Path tempFile = file.getParent().resolve(file.getFileName().toString() + ".tmp");
		Files.createDirectories(file.getParent());

		Object unwrappedData = unwrap(data);
		if (file.toString().endsWith(".toml")) {
			try {
				var table = JTOML.toToml(unwrappedData);
				JTOML.write(tempFile, table);
			} catch (Exception e) {
				log.warn("JToml failed to write TOML, using fallback: " + e.getMessage());
				Files.writeString(tempFile, toTomlString(unwrappedData), StandardCharsets.UTF_8);
			}
		} else {
			try (var writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
				yaml.dump(unwrappedData, writer);
			}
		}
		Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}

	private String toTomlString(Object data) {
		StringBuilder sb = new StringBuilder();
		if (data instanceof Map m) {
			for (Object key : m.keySet()) {
				Object val = m.get(key);
				if (val instanceof List l) {
					for (Object item : l) {
						sb.append("[[").append(key).append("]]\n");
						sb.append(toTomlProperties(item));
						sb.append("\n");
					}
				} else {
					sb.append(key).append(" = ").append(formatTomlValue(val)).append("\n");
				}
			}
		}
		return sb.toString();
	}

	private String toTomlProperties(Object data) {
		StringBuilder sb = new StringBuilder();
		if (data instanceof Map m) {
			for (Object key : m.keySet()) {
				sb.append(key).append(" = ").append(formatTomlValue(m.get(key))).append("\n");
			}
		}
		return sb.toString();
	}

	private String formatTomlValue(Object val) {
		if (val instanceof String s) {
			return "\"" + s.replace("\"", "\\\"") + "\"";
		}
		if (val instanceof Boolean b) {
			return b.toString();
		}
		if (val instanceof Number n) {
			return n.toString();
		}
		if (val == null) {
			return "\"\""; // TOML has no null, use empty string
		}
		return "\"" + val.toString().replace("\"", "\\\"") + "\"";
	}

	@Override
	public synchronized void deleteTaxonomy(String slug) throws IOException {
		Path file = getTaxonomyDefinitionFile();
		if (!Files.exists(file)) {
			return;
		}

		List<Object> taxonomiesData;
		if (file.toString().endsWith(".toml")) {
			var source = TomlConfigSource.build(file);
			taxonomiesData = source.getList("taxonomies");
		} else {
			var source = YamlConfigSource.build(file);
			taxonomiesData = source.getList("taxonomies");
		}

		List<Taxonomy> allDefinitions = taxonomiesData.stream()
				.map(item -> GSONProvider.GSON.fromJson(GSONProvider.GSON.toJson(item), Taxonomy.class))
				.collect(Collectors.toList());

		allDefinitions.removeIf(t -> t.getSlug().equals(slug));

		Map<String, Object> data = new HashMap<>();
		data.put("taxonomies", allDefinitions.stream().map(this::toPlainMapWithoutValues).collect(Collectors.toList()));

		saveAtomic(file, data);

		Path valuesFile = getTaxonomyValuesFile(slug);
		Files.deleteIfExists(valuesFile);
	}

	@Override
	public synchronized void saveValue(String taxonomySlug, Value value) throws IOException {
		Optional<Taxonomy> taxoOpt = forSlug(taxonomySlug);
		if (taxoOpt.isPresent()) {
			Taxonomy taxo = taxoOpt.get();
			taxo.getValues().put(value.getId(), value);

			Map<String, Object> data = new HashMap<>();
			data.put("values", taxo.getValues().values().stream().map(this::toPlainMap).collect(Collectors.toList()));

			Path file = getTaxonomyValuesFile(taxonomySlug);
			saveAtomic(file, data);
		}
	}

	@Override
	public synchronized void deleteValue(String taxonomySlug, String valueId) throws IOException {
		Optional<Taxonomy> taxoOpt = forSlug(taxonomySlug);
		if (taxoOpt.isPresent()) {
			Taxonomy taxo = taxoOpt.get();
			taxo.getValues().remove(valueId);

			Map<String, Object> data = new HashMap<>();
			data.put("values", taxo.getValues().values().stream().map(this::toPlainMap).collect(Collectors.toList()));

			Path file = getTaxonomyValuesFile(taxonomySlug);
			saveAtomic(file, data);
		}
	}
}
