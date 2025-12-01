package com.condation.cms.core.configuration.source;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnvConfigSourceTest {

    private Path tempDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-env");
    }

    @Test
    public void testLoadFromDotEnv() throws IOException {
        Path dotenvFile = tempDir.resolve(".env");
        try (FileWriter writer = new FileWriter(dotenvFile.toFile())) {
            writer.write("DATABASE_HOST=localhost\n");
            writer.write("DATABASE_PORT=5432\n");
        }

        System.setProperty("user.dir", tempDir.toString());
        EnvConfigSource configSource = new EnvConfigSource();

        assertEquals("localhost", configSource.getString("database.host"));
        assertEquals("5432", configSource.getString("database.port"));
    }

    @Test
    public void testSystemVariableOverride() throws IOException {
        Path dotenvFile = tempDir.resolve(".env");
        try (FileWriter writer = new FileWriter(dotenvFile.toFile())) {
            writer.write("DATABASE_HOST=localhost\n");
        }

        System.setProperty("user.dir", tempDir.toString());
        System.setProperty("DATABASE_HOST", "remotehost");
        EnvConfigSource configSource = new EnvConfigSource();

        assertEquals("remotehost", configSource.getString("database.host"));
    }

    @Test
    public void testNestedProperties() {
        System.setProperty("DATABASE_CREDENTIALS_USERNAME", "testuser");
        System.setProperty("DATABASE_CREDENTIALS_PASSWORD", "testpass");
        EnvConfigSource configSource = new EnvConfigSource();

        Map<String, Object> credentials = configSource.getMap("database.credentials");
        assertEquals("testuser", credentials.get("username"));
        assertEquals("testpass", credentials.get("password"));
    }
}
