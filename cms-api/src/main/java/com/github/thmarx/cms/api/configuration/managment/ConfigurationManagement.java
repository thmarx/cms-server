package com.github.thmarx.cms.api.configuration;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.configuration.configs.SiteConfiguration;
import com.github.thmarx.cms.api.configuration.configs.TaxonomyConfiguration;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.ConfigurationFileChanged;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
public class ConfigurationManagement implements Runnable {

	final DB db;
	@Getter
	final Configuration configuration;
	final ScheduledExecutorService scheduler;
	final EventBus eventBus;

	private List<ConfigurationResource> watched_configurations = new ArrayList<>();

	public void init() throws IOException {
		// init config files
		addPathToWatch(db.getFileSystem().resolve("site.yaml"), SiteConfiguration.class);
		var taxoPath = db.getFileSystem().resolve("config/taxonomy.yaml");
		addPathToWatch(taxoPath, TaxonomyConfiguration.class);
		if (Files.exists(taxoPath.getParent())) {
			Files.list(taxoPath.getParent())
					.filter(path -> Constants.TAXONOMY_VALUE.matcher(path.getFileName().toString()).matches())
					.forEach(path -> {
						try {
							addPathToWatch(path, TaxonomyConfiguration.class);
						} catch (IOException ioe) {
							log.error(null, ioe);
						}
					});
		}

		// setup scheduler
		scheduler.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MINUTES);
	}

	private void addPathToWatch(final Path configFile, final Class<? extends Config> configClass) throws IOException {
		if (!Files.exists(configFile)) {
			return;
		}
		watched_configurations.add(
				new ConfigurationResource(configFile, configClass, Files.getLastModifiedTime(configFile).toMillis())
		);
	}

	@Override
	public void run() {
		log.trace("check for modified configurations {}", db.getFileSystem().resolve(".").toString());
		watched_configurations.forEach(config -> {
			try {
				var tempMod = Files.getLastModifiedTime(config.configFile).toMillis();

				if (tempMod != config.lastModified) {
					log.debug("modified: " + config.configFile.getFileName().toString());
					config.setLastModified(tempMod);

					eventBus.publish(new ConfigurationFileChanged(config.configClass));
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		});
	}

	@AllArgsConstructor
	@Data
	public static class ConfigurationResource {

		Path configFile;
		Class<? extends Config> configClass;
		long lastModified;
	}
}
