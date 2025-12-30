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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PeriodicReload implements ReloadStrategy {

    private final long period;
    private final TimeUnit timeUnit;
    private final ScheduledExecutorService scheduler;

    public PeriodicReload(long period, TimeUnit timeUnit) {
        this.period = period;
        this.timeUnit = timeUnit;

        ThreadFactory daemonThreadFactory = (Runnable r) -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);
    }

    @Override
    public void register(IConfiguration configuration, List<ConfigSource> sources) {
        scheduler.scheduleAtFixedRate(() -> {
            log.trace("Periodic reload of config {} triggered", configuration.id());
            configuration.reload();
        }, period, period, timeUnit);
    }
}
