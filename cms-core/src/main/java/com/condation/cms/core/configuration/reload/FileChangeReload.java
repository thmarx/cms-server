package com.condation.cms.core.configuration.reload;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileChangeReload implements ReloadStrategy {

    private WatchService watchService;
    private Thread watchThread;
    private final Set<Path> watchedFileNames = new HashSet<>();

    public FileChangeReload() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            log.error("Failed to create WatchService. File change reload strategy will be disabled.", e);
        }
    }

    @Override
    public void register(IConfiguration configuration, List<ConfigSource> sources) {
        if (watchService == null) {
            return;
        }

        Set<Path> registeredDirs = new HashSet<>();
        for (ConfigSource source : sources) {
            Path configFile = source.getConfigFile();
            if (configFile != null && Files.exists(configFile)) {
                watchedFileNames.add(configFile.getFileName());
                Path configDir = configFile.getParent();
                if (registeredDirs.add(configDir)) {
                     try {
                        configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    } catch (IOException e) {
                        log.error("Failed to register watch service for directory: {}", configDir, e);
                    }
                }
            }
        }

        if (watchedFileNames.isEmpty()) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Failed to close unused WatchService.", e);
            }
            return;
        }

        this.watchThread = new Thread(() -> {
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changedFileName = (Path) event.context();
                        if (watchedFileNames.contains(changedFileName)) {
                            log.info("Reloading configuration due to file change: {}", changedFileName);
                            configuration.reload();
                            break; // Reload once per batch of events
                        }
                    }
                    if (!key.reset()) {
                        break; // Key is no longer valid
                    }
                }
            } catch (InterruptedException | ClosedWatchServiceException e) {
                log.info("FileChangeReload watch thread interrupted.");
                Thread.currentThread().interrupt();
            }
        });
        this.watchThread.setDaemon(true);
        this.watchThread.start();
    }
}
