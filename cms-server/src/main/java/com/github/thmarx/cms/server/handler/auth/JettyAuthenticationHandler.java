package com.github.thmarx.cms.server.handler.auth;

/*-
 * #%L
 * cms-server
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
import com.github.thmarx.cms.api.utils.RequestUtil;
import com.github.thmarx.cms.auth.services.AuthService;
import com.github.thmarx.cms.auth.services.UserService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.StringTokenizer;
import com.google.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
public class JettyAuthenticationHandler extends Handler.Abstract {

	private final AuthService authService;
	private final UserService userService;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		var uri = "/" + RequestUtil.getContentPath(request);
		
		Optional<AuthService.Auth> authOpt = authService.load();
		if (authOpt.isEmpty()) {
			return false;
		}
		Optional<AuthService.AuthPath> authPathOpt = authOpt.get().find(uri);
		if (authPathOpt.isEmpty()) {
			return false;
		}
		var authPath = authPathOpt.get();

		String authHeader = request.getHeaders().get(HttpHeader.AUTHORIZATION);
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.getDecoder().decode(st.nextToken()));

						int p = credentials.indexOf(":");
						if (p != -1) {
							String username = credentials.substring(0, p).trim();
							String password = credentials.substring(p + 1).trim();

							var userOpt = userService.login(UserService.Realm.of(authPath.getRealm()), username, password);
							
							if (userOpt.isEmpty()){
								unauthorized(response, callback, authPath.getRealm());
								return true;
							}
							
							if (authPath.allowed(userOpt.get())) {
								return false;
							}
							
						} else {
							unauthorized(response, callback, authPath.getRealm());
							return true;
						}
					} catch (UnsupportedEncodingException e) {
						throw new Error("Couldn't retrieve authentication", e);
					}
				}
			}
		}
		unauthorized(response, callback, authPath.getRealm());
		return true;
	}

	private void unauthorized(Response response, Callback callback, String realm) throws IOException {
		response.getHeaders().add("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		response.setStatus(401);
		callback.succeeded();
	}

}
