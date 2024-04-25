package com.github.thmarx.cms.auth.services;

/*-
 * #%L
 * cms-auth
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

import java.nio.file.Path;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 *
 * @author t.marx
 */
public class AuthServiceTest {

	@Test
	public void no_auth() {
		
		var authService = new AuthService(Path.of("src/test/resources/hosts/none"));
		
		var auth = authService.load();
		Assertions.assertThat(auth).isEmpty();
	}
	
	@Test
	public void load_auth() {
		
		var authService = new AuthService(Path.of("src/test/resources/hosts/demo"));
		
		var auth = authService.load();
		Assertions.assertThat(auth).isPresent();
		
		Optional<AuthService.AuthPath> find = auth.get().find("/secured");
		
		Assertions.assertThat(find).isPresent();
	}
}
