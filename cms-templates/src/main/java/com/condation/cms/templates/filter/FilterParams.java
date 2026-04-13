package com.condation.cms.templates.filter;

/*-
 * #%L
 * CMS Templates
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

import java.util.Arrays;

/**
 * Type-safe wrapper for filter parameters.
 * Provides convenient access methods with type checking and conversion.
 */
public class FilterParams {

	private final Object[] params;

	public FilterParams(Object... params) {
		this.params = params != null ? params : new Object[0];
	}

	/**
	 * Returns the number of parameters.
	 */
	public int size() {
		return params.length;
	}

	/**
	 * Checks if there are no parameters.
	 */
	public boolean isEmpty() {
		return params.length == 0;
	}

	/**
	 * Gets a parameter at the specified index with type checking.
	 *
	 * @param index the parameter index
	 * @param type  the expected type
	 * @return the parameter cast to the expected type
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 * @throws ClassCastException        if parameter cannot be cast to expected type
	 */
	public <T> T get(int index, Class<T> type) {
		if (index < 0 || index >= params.length) {
			throw new IndexOutOfBoundsException("Parameter index " + index + " out of bounds (size: " + params.length + ")");
		}
		Object param = params[index];
		if (param == null) {
			return null;
		}
		if (!type.isInstance(param)) {
			throw new ClassCastException("Parameter at index " + index + " is " + param.getClass().getName() + ", expected " + type.getName());
		}
		return type.cast(param);
	}

	/**
	 * Gets a String parameter at the specified index.
	 *
	 * @param index the parameter index
	 * @return the parameter as String, or null if parameter is null
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 */
	public String getString(int index) {
		Object param = getRaw(index);
		return param != null ? param.toString() : null;
	}

	/**
	 * Gets a String parameter with a default value.
	 *
	 * @param index        the parameter index
	 * @param defaultValue the default value if parameter is missing or null
	 * @return the parameter as String, or defaultValue
	 */
	public String getString(int index, String defaultValue) {
		if (index < 0 || index >= params.length || params[index] == null) {
			return defaultValue;
		}
		return params[index].toString();
	}

	/**
	 * Gets an Integer parameter at the specified index.
	 *
	 * @param index the parameter index
	 * @return the parameter as Integer
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 * @throws NumberFormatException     if parameter cannot be converted to Integer
	 */
	public Integer getInt(int index) {
		Object param = getRaw(index);
		if (param == null) {
			return null;
		}
		if (param instanceof Number) {
			return ((Number) param).intValue();
		}
		return Integer.parseInt(param.toString());
	}

	/**
	 * Gets an Integer parameter with a default value.
	 *
	 * @param index        the parameter index
	 * @param defaultValue the default value if parameter is missing or null
	 * @return the parameter as Integer, or defaultValue
	 */
	public int getInt(int index, int defaultValue) {
		try {
			Integer value = getInt(index);
			return value != null ? value : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Gets a Long parameter at the specified index.
	 *
	 * @param index the parameter index
	 * @return the parameter as Long
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 * @throws NumberFormatException     if parameter cannot be converted to Long
	 */
	public Long getLong(int index) {
		Object param = getRaw(index);
		if (param == null) {
			return null;
		}
		if (param instanceof Number) {
			return ((Number) param).longValue();
		}
		return Long.parseLong(param.toString());
	}

	/**
	 * Gets a Long parameter with a default value.
	 *
	 * @param index        the parameter index
	 * @param defaultValue the default value if parameter is missing or null
	 * @return the parameter as Long, or defaultValue
	 */
	public long getLong(int index, long defaultValue) {
		try {
			Long value = getLong(index);
			return value != null ? value : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Gets a Boolean parameter at the specified index.
	 *
	 * @param index the parameter index
	 * @return the parameter as Boolean
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 */
	public Boolean getBoolean(int index) {
		Object param = getRaw(index);
		if (param == null) {
			return null;
		}
		if (param instanceof Boolean) {
			return (Boolean) param;
		}
		return Boolean.parseBoolean(param.toString());
	}

	/**
	 * Gets a Boolean parameter with a default value.
	 *
	 * @param index        the parameter index
	 * @param defaultValue the default value if parameter is missing or null
	 * @return the parameter as Boolean, or defaultValue
	 */
	public boolean getBoolean(int index, boolean defaultValue) {
		try {
			Boolean value = getBoolean(index);
			return value != null ? value : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Gets a raw parameter at the specified index without type conversion.
	 *
	 * @param index the parameter index
	 * @return the raw parameter
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 */
	public Object getRaw(int index) {
		if (index < 0 || index >= params.length) {
			throw new IndexOutOfBoundsException("Parameter index " + index + " out of bounds (size: " + params.length + ")");
		}
		return params[index];
	}

	/**
	 * Gets all parameters as raw Object array.
	 *
	 * @return the raw parameters array
	 */
	public Object[] getRawArray() {
		return params;
	}

	/**
	 * Checks if a parameter exists at the specified index.
	 *
	 * @param index the parameter index
	 * @return true if parameter exists
	 */
	public boolean has(int index) {
		return index >= 0 && index < params.length;
	}

	@Override
	public String toString() {
		return "FilterParams" + Arrays.toString(params);
	}
}
