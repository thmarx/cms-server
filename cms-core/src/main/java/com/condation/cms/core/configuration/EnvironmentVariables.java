package com.condation.cms.core.configuration;

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

import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.core.utils.EnvUtil;
import java.util.Map;

public class EnvironmentVariables {

    private static EnvironmentVariables instance;
    private final Map<String, Object> variables;

    private EnvironmentVariables() {
        this.variables = EnvUtil.load(ServerUtil.getHome().toAbsolutePath().toString());
    }

    public static synchronized EnvironmentVariables getInstance() {
        if (instance == null) {
            instance = new EnvironmentVariables();
        }
        return instance;
    }

    public Object getVariable(String name) {
        return MapUtil.getValue(variables, name.toLowerCase());
    }

    public String getString(String name) {
        Object value = getVariable(name);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
