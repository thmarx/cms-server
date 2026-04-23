package com.condation.cms.api.db.taxonomy;

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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public interface TaxonomyStore {

	List<Taxonomy> all();

	Optional<Taxonomy> forSlug(String slug);

	void saveTaxonomy(Taxonomy taxonomy) throws IOException;

	void deleteTaxonomy(String slug) throws IOException;

	void saveValue(String taxonomySlug, Value value) throws IOException;

	void deleteValue(String taxonomySlug, String valueId) throws IOException;
}
