package com.condation.cms.server.filter.metrics;

/*-
 * #%L
 * CMS Server
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
import com.condation.cms.api.SiteProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
public class PipelineRequestMetricsFilter extends Handler.Wrapper {

    private final MeterRegistry meterRegistry;

    private final Counter requestCounter;
    private final Timer timer;
    private final Counter errorCounter;

    public PipelineRequestMetricsFilter(final Handler handler, final String siteId,
            final MeterRegistry meterRegistry, final String pipeline) {
        super(handler);
        this.meterRegistry = meterRegistry;

        this.requestCounter = Counter.builder("com.condation.http.pipeline.requests")
                .tag("site", siteId)
                .tag("pipeline", pipeline)
                .register(meterRegistry);
        this.timer = Timer.builder("com.condation.http.pipeline.duration")
                .tag("site", siteId)
                .tag("pipeline", pipeline)
                .register(meterRegistry);
        this.errorCounter = Counter.builder("com.condation.http.pipeline.errors")
                .tag("site", siteId)
                .tag("pipeline", pipeline)
                .register(meterRegistry);
    }

    @Override
    public boolean handle(Request rqst, Response rspns, Callback clbck) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry);
        requestCounter.increment();
        try {
            return super.handle(rqst, rspns, clbck);
        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        } finally {
            sample.stop(timer);
        }
    }

}
