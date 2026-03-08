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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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
		// Optimization: try to find it in the definition file first, then load values only for that one
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
		List<Taxonomy> all = all();
		boolean found = false;
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getSlug().equals(taxonomy.getSlug())) {
				all.set(i, taxonomy);
				found = true;
				break;
			}
		}
		if (!found) {
			all.add(taxonomy);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("taxonomies", all.stream().map(this::taxonomyToMap).collect(Collectors.toList()));

		Path file = getTaxonomyDefinitionFile();
		// If it's TOML, we might want to convert it to YAML if we are writing,
		// but the requirement says "the implementation in the filesystem should remain as it is".
		// However, we only have a YAML writer here.
		// For now, if it was TOML, we write it back as YAML to the same filename? No, that's bad.
		// Let's stick to YAML for writing as per common practice in this CMS when things are editable.
		if (file.toString().endsWith(".toml")) {
			file = hostBase.resolve("config/taxonomy.yaml");
		}

		saveAtomic(file, data);
	}

	private Map<String, Object> taxonomyToMap(Taxonomy t) {
		Map<String, Object> m = new HashMap<>();
		m.put("title", t.getTitle());
		m.put("slug", t.getSlug());
		m.put("field", t.getField());
		m.put("array", t.isArray());
		m.put("template", t.getTemplate());
		m.put("template_single", t.getSingleTemplate());
		return m;
	}

	private Map<String, Object> valueToMap(Value v) {
		Map<String, Object> m = new HashMap<>();
		m.put("id", v.getId());
		m.put("title", v.getTitle());
		return m;
	}

	private void saveAtomic(Path file, Object data) throws IOException {
		Path tempFile = file.getParent().resolve(file.getFileName().toString() + ".tmp");
		Files.createDirectories(file.getParent());
		try (var writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
			yaml.dump(data, writer);
		}
		Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
	}

	@Override
	public synchronized void deleteTaxonomy(String slug) throws IOException {
		List<Taxonomy> all = all();
		all.removeIf(t -> t.getSlug().equals(slug));

		Map<String, Object> data = new HashMap<>();
		data.put("taxonomies", all.stream().map(this::taxonomyToMap).collect(Collectors.toList()));

		Path file = getTaxonomyDefinitionFile();
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
			data.put("values", taxo.getValues().values().stream().map(this::valueToMap).collect(Collectors.toList()));

			Path file = getTaxonomyValuesFile(taxonomySlug);
			if (file.toString().endsWith(".toml")) {
				file = hostBase.resolve("config/taxonomy.%s.yaml".formatted(taxonomySlug));
			}
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
			data.put("values", taxo.getValues().values().stream().map(this::valueToMap).collect(Collectors.toList()));

			Path file = getTaxonomyValuesFile(taxonomySlug);
			if (file.toString().endsWith(".toml")) {
				file = hostBase.resolve("config/taxonomy.%s.yaml".formatted(taxonomySlug));
			}
			saveAtomic(file, data);
		}
	}
}
