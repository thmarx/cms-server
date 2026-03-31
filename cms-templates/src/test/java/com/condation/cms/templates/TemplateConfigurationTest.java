package com.condation.cms.templates;

/*-
 * #%L
 * templates
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

import com.condation.cms.templates.error.LenientErrorHandler;
import com.condation.cms.templates.error.StrictErrorHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for TemplateConfiguration's configurable parameters.
 */
public class TemplateConfigurationTest {

	@Test
	public void testDevModeDefaults() {
		TemplateConfiguration config = new TemplateConfiguration(true);

		Assertions.assertThat(config.isDevMode()).isTrue();
		Assertions.assertThat(config.getExpressionCacheSize()).isEqualTo(512);
		Assertions.assertThat(config.isJexlSafeMode()).isFalse();
		Assertions.assertThat(config.isJexlSilent()).isFalse();
		Assertions.assertThat(config.isJexlStrict()).isFalse();
		Assertions.assertThat(config.getMaxRenderDepth()).isEqualTo(100);
		Assertions.assertThat(config.getErrorHandler()).isInstanceOf(StrictErrorHandler.class);
	}

	@Test
	public void testProdModeDefaults() {
		TemplateConfiguration config = new TemplateConfiguration(false);

		Assertions.assertThat(config.isDevMode()).isFalse();
		Assertions.assertThat(config.getExpressionCacheSize()).isEqualTo(1024);
		Assertions.assertThat(config.isJexlSafeMode()).isTrue();
		Assertions.assertThat(config.isJexlSilent()).isTrue();
		Assertions.assertThat(config.isJexlStrict()).isFalse();
		Assertions.assertThat(config.getMaxRenderDepth()).isEqualTo(100);
		Assertions.assertThat(config.getErrorHandler()).isInstanceOf(LenientErrorHandler.class);
	}

	@Test
	public void testCustomConfiguration() {
		TemplateConfiguration config = new TemplateConfiguration(false);

		// Override defaults
		config.setExpressionCacheSize(2048);
		config.setMaxRenderDepth(200);
		config.setJexlStrict(true);

		Assertions.assertThat(config.getExpressionCacheSize()).isEqualTo(2048);
		Assertions.assertThat(config.getMaxRenderDepth()).isEqualTo(200);
		Assertions.assertThat(config.isJexlStrict()).isTrue();
	}

	@Test
	public void testFluentAPI() {
		TemplateConfiguration config = new TemplateConfiguration(false);

		// Test fluent chaining
		TemplateConfiguration result = config.setDevMode(true);

		Assertions.assertThat(result).isSameAs(config);
		Assertions.assertThat(config.isDevMode()).isTrue();
	}
}
