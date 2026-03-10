package com.condation.cms.core.db.taxonomy;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileTaxonomyStoreTest {

	@TempDir
	Path tempDir;

	@Test
	public void testSaveAndLoad() throws IOException {
		FileTaxonomyStore store = new FileTaxonomyStore(tempDir);

		Taxonomy taxo = new Taxonomy("Marken", "marken", "taxonomy.brands");
		taxo.setArray(true);
		taxo.setSingleTemplate("taxonomy.single.marken.html");

		store.saveTaxonomy(taxo);

		Path defFile = tempDir.resolve("config/taxonomy.yaml");
		Assertions.assertTrue(Files.exists(defFile), "Definition file should exist");

		List<Taxonomy> all = store.all();
		Assertions.assertEquals(1, all.size());
		Assertions.assertEquals("marken", all.get(0).getSlug());

		Value v1 = new Value("s-oliver", "s.Oliver");
		store.saveValue("marken", v1);

		Path valFile = tempDir.resolve("config/taxonomy.marken.yaml");
		Assertions.assertTrue(Files.exists(valFile), "Values file should exist");

		Taxonomy loaded = store.forSlug("marken").get();
		Assertions.assertEquals(1, loaded.getValues().size());
		Assertions.assertTrue(loaded.getValues().containsKey("s-oliver"));

		// Verify that the definition file does NOT contain values
		String defContent = Files.readString(defFile);
		Assertions.assertFalse(defContent.contains("s-oliver"), "Definition file should not contain values");
	}

	@Test
	public void testTomlSupport() throws IOException {
		Files.createDirectories(tempDir.resolve("config"));
		Path tomlFile = tempDir.resolve("config/taxonomy.toml");
		Files.writeString(tomlFile, "[[taxonomies]]\nslug = \"marken\"\ntitle = \"Marken\"\nfield = \"taxonomy.brands\"");

		FileTaxonomyStore store = new FileTaxonomyStore(tempDir);
		List<Taxonomy> all = store.all();
		Assertions.assertEquals(1, all.size());
		Assertions.assertEquals("marken", all.get(0).getSlug());

		Taxonomy taxo = all.get(0);
		taxo.setTitle("New Marken");
		store.saveTaxonomy(taxo);

		String content = Files.readString(tomlFile);
		Assertions.assertTrue(content.contains("New Marken"), "TOML file should be updated");
	}
}
