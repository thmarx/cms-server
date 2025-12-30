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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileChangeReloadTest {

    @Test
    public void testFileChangeReload_triggersOnCorrectFile(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Prepare
        Path configFile = tempDir.resolve("test.properties");
        Files.createFile(configFile);

        AtomicBoolean reloaded = new AtomicBoolean(false);
        IConfiguration configuration = mock(IConfiguration.class);
        when(configuration.id()).thenReturn("test-config");
        doAnswer(invocation -> {
            reloaded.set(true);
            return null;
        }).when(configuration).reload();

        ConfigSource configSource = mock(ConfigSource.class);
        when(configSource.getConfigFile()).thenReturn(configFile);

        FileChangeReload fileChangeReload = new FileChangeReload();
        fileChangeReload.register(configuration, Collections.singletonList(configSource));

        // Act
        Files.writeString(configFile, "test=value");

        // Assert
        Thread.sleep(200); // Give the WatchService time to react
        assertTrue(reloaded.get(), "Configuration should have been reloaded after the correct file was changed.");
    }

    @Test
    public void testFileChangeReload_doesNotTriggerOnUnrelatedFile(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Prepare
        Path configFile = tempDir.resolve("test.properties");
        Path unrelatedFile = tempDir.resolve("unrelated.txt");
        Files.createFile(configFile);
        Files.createFile(unrelatedFile);

        AtomicBoolean reloaded = new AtomicBoolean(false);
        IConfiguration configuration = mock(IConfiguration.class);
        when(configuration.id()).thenReturn("test-config");
        doAnswer(invocation -> {
            reloaded.set(true);
            return null;
        }).when(configuration).reload();

        ConfigSource configSource = mock(ConfigSource.class);
        when(configSource.getConfigFile()).thenReturn(configFile);

        FileChangeReload fileChangeReload = new FileChangeReload();
        fileChangeReload.register(configuration, Collections.singletonList(configSource));

        // Act
        Files.writeString(unrelatedFile, "some data");

        // Assert
        Thread.sleep(200); // Give the WatchService time to react
        assertFalse(reloaded.get(), "Configuration should NOT have been reloaded after an unrelated file was changed.");
    }
}
