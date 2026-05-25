package com.condation.cms.api.hooks;

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

import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.annotations.Filter;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HookSystemFlexibleTest {

	private HookSystem hookSystem;

	@BeforeEach
	public void setup() {
		hookSystem = new HookSystem();
	}

	@Test
	void test_flexible_action () {
		var actionObject = new MyFlexibleActions();
		hookSystem.register(actionObject);

		hookSystem.execute("test/flexible/action1", Map.of("name", "world"));

		Assertions.assertThat(actionObject.receivedName).isEqualTo("world");
	}

	@Test
	void test_flexible_filter () {
		var filterObject = new MyFlexibleFilters();
		hookSystem.register(filterObject);

		var context = hookSystem.filter("test/flexible/filter1", "hello");
		Assertions.assertThat(context.value()).isEqualTo("HELLO");
	}

	public class MyFlexibleActions {
		public String receivedName;

		@Action("test/flexible/action1")
		public void action1 (String name) {
			this.receivedName = name;
		}
	}

	public class MyFlexibleFilters {
		@Filter("test/flexible/filter1")
		public String filter1 (String input) {
			return input.toUpperCase();
		}
	}
}
