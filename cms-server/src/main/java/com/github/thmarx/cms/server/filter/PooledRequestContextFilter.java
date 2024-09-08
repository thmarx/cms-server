package com.github.thmarx.cms.server.filter;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.PerformanceProperties;
import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.feature.features.IsPreviewFeature;
import com.github.thmarx.cms.api.feature.features.RequestFeature;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import com.github.thmarx.cms.api.utils.HTTPUtil;
import com.github.thmarx.cms.api.utils.RequestUtil;
import com.github.thmarx.cms.request.RequestContextFactory;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import stormpot.Allocator;
import stormpot.Expiration;
import stormpot.Pool;
import stormpot.Poolable;
import stormpot.Slot;
import stormpot.Timeout;

/**
 *
 * @author t.marx
 */
@Slf4j
public class PooledRequestContextFilter extends Handler.Wrapper {

	public static final String REQUEST_CONTEXT = "_requestContext";

	Pool<MyPoolable> requestContextPool;

	private final PerformanceProperties properties;
	
	public PooledRequestContextFilter(final Handler handler, final RequestContextFactory requestContextFactory,
			final PerformanceProperties properties) {
		super(handler);
		this.properties = properties;

		requestContextPool = Pool.from(new MyAllocator(requestContextFactory))
				.setExpiration(Expiration.after(properties.pool_expire(), TimeUnit.SECONDS))
				.setSize(properties.pool_size())
				.build();
	}
	 
	@Override
	public boolean handle(Request httpRequest, Response rspns, Callback clbck) throws Exception {
		var requestContextPoolable = requestContextPool.claim(new Timeout(Duration.ofSeconds(1)));
		try {

			var requestContext = requestContextPoolable.requestContext;

			var uri = RequestUtil.getContentPath(httpRequest);
			var queryParameters = HTTPUtil.queryParameters(httpRequest.getHttpURI().getQuery());
			var contextPath = httpRequest.getContext().getContextPath();

			requestContext.add(RequestFeature.class, new RequestFeature(contextPath, uri, queryParameters, httpRequest));
			if (ServerContext.IS_DEV && queryParameters.containsKey("preview")) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature());
			}

			ThreadLocalRequestContext.REQUEST_CONTEXT.set(requestContext);

			httpRequest.setAttribute(REQUEST_CONTEXT, requestContext);

			return super.handle(httpRequest, rspns, clbck);

		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
			requestContextPoolable.release();
		}
	}


	@RequiredArgsConstructor
	private static class MyAllocator implements Allocator<MyPoolable> {

		private final RequestContextFactory requestContextFactory;

		@Override
		public MyPoolable allocate(Slot slot) throws Exception {
			log.info("allocate");
			return new MyPoolable(slot, requestContextFactory.create());
		}

		@Override
		public void deallocate(MyPoolable poolable) throws Exception {
			log.info("deallocate");
			poolable.close();
		}

	}

	@RequiredArgsConstructor
	private static class MyPoolable implements Poolable, Closeable {

		private final Slot slot;
		@Getter
		private final RequestContext requestContext;

		@Override
		public void release() {
			slot.release(this);
		}

		@Override
		public void close() throws IOException {
			try {
				requestContext.close();
			} catch (Exception ex) {
				log.error("", ex);
			}
		}

	}

}
