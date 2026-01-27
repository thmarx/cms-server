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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigValueProcessor {

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{env:([a-zA-Z0-9_.-]+)\\}");

    @SuppressWarnings("unchecked")
    public static <T> T process(T value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (T) processString((String) value);
        } else if (value instanceof List) {
            return (T) processList((List<?>) value);
        } else if (value instanceof Map) {
            return (T) processMap((Map<String, ?>) value);
        } else {
            return value;
        }
    }

    private static String processString(String value) {
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String envValue = EnvironmentVariables.getInstance().getString(varName);
            if (envValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
            } else {
                // Keep the placeholder if the variable is not found
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static List<?> processList(List<?> list) {
        return list.stream()
                .map(ConfigValueProcessor::process)
                .collect(Collectors.toList());
    }

    private static Map<String, ?> processMap(Map<String, ?> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> process(entry.getValue())
                ));
    }
}
