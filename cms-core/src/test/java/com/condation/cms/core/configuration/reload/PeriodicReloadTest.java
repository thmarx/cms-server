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

import com.condation.cms.core.configuration.IConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeriodicReloadTest {

    @Test
    public void testPeriodicReload() throws InterruptedException {
        // Prepare
        AtomicBoolean reloaded = new AtomicBoolean(false);
        IConfiguration configuration = mock(IConfiguration.class);
        when(configuration.id()).thenReturn("test-config");
        doAnswer(invocation -> {
            reloaded.set(true);
            return null;
        }).when(configuration).reload();

        PeriodicReload periodicReload = new PeriodicReload(100, TimeUnit.MILLISECONDS);
        periodicReload.register(configuration, Collections.emptyList());

        // Act
        Thread.sleep(150);

        // Assert
        assertTrue(reloaded.get(), "Configuration should have been reloaded periodically.");
    }
}
