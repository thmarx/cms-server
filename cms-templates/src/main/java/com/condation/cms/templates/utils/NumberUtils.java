/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.templates.utils;

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

public class NumberUtils {

    /**
     * Konvertiert ein Objekt in eine Zahl (int oder long).
     * Falls das Objekt null ist oder keine Zahl darstellt, wird der Default-Wert zurückgegeben.
     *
     * @param obj          Das zu prüfende Objekt
     * @param defaultValue Der Standardwert (bestimmt auch den Rückgabetyp)
     * @return Das konvertierte Objekt oder der Default-Wert
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> T castToNumber(Object obj, T defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        // Fall 1: Das Objekt ist bereits eine Instanz einer Zahl (Integer, Long, Double, etc.)
        if (obj instanceof Number) {
            Number number = (Number) obj;
            if (defaultValue instanceof Integer) {
                return (T) Integer.valueOf(number.intValue());
            } else if (defaultValue instanceof Long) {
                return (T) Long.valueOf(number.longValue());
            }
        }

        // Fall 2: Das Objekt ist ein String (z.B. "123"), der geparsed werden kann
        if (obj instanceof String) {
            try {
                String str = (String) obj;
                if (defaultValue instanceof Integer) {
                    return (T) Integer.valueOf(Integer.parseInt(str));
                } else if (defaultValue instanceof Long) {
                    return (T) Long.valueOf(Long.parseLong(str));
                }
            } catch (NumberFormatException e) {
                // String war keine gültige Zahl -> ignoriert, liefert Default-Wert
            }
        }

        // Fall 3: Weder Zahl noch passender String -> Default-Wert
        return defaultValue;
    }
}
