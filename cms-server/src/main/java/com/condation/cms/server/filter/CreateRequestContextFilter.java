package com.condation.cms.server.filter;

/*-
 * #%L
 * cms-server
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


import com.condation.cms.api.Constants;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.filesystem.virtual.SiteContext;
import com.condation.cms.request.RequestContextFactory;
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
public class CreateRequestContextFilter extends Handler.Wrapper {

	private final RequestContextFactory requestContextFactory;

	public CreateRequestContextFilter(final Handler handler, final RequestContextFactory requestContextFactory) {
		super(handler);
		this.requestContextFactory = requestContextFactory;
	}

	@Override
	public boolean handle(final Request httpRequest, final Response rspns, final Callback clbck) throws Exception {
		try (var requestContext = requestContextFactory.createContext()) {
			httpRequest.setAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME, requestContext);

			return ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext).call(() -> {
				
				SiteContext siteContext = new SiteContext(
						requestContext.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties().id(),
						requestContext.get(InjectorFeature.class).injector().getInstance(DB.class),
						requestContext.get(ThemeFeature.class).theme()
				);
				return ScopedValue.where(SiteContext.SCOPE, siteContext).call(() -> {
					return super.handle(httpRequest, rspns, clbck);
				});
			});
			
		} catch (Exception e) {
			log.error("", e);
			Response.writeError(httpRequest, rspns, clbck, e);
			return true;
		}
	}

}
