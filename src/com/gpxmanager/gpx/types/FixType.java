/*
 * FixType.java
 *
 * Copyright (c) 2012, AlternativeVision. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.gpxmanager.gpx.types;

/**
 * <p>Type of GPS fix. Value comes from list: {'none'|'2d'|'3d'|'dgps'|'pps'}</p>
 * <br>
 * <ul>
 * <li>none = GPS had no fix.</li>
 * <li>pps = military signal used</li>
 * </ul>
 * <p>To signify "the fix info is unknown", leave out fixType entirely.</p>
 */
public enum FixType {
    NONE("none"),
    TWO_D("2d"),
    THREE_D("3d"),
    DGPS("dgps"),
    PPS("pps");

    private final String value;

    FixType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FixType returnType(String value) {
        if (NONE.getValue().equals(value)) {
            return NONE;
        } else if (TWO_D.getValue().equals(value)) {
            return TWO_D;
        } else if (THREE_D.getValue().equals(value)) {
            return THREE_D;
        } else if (DGPS.getValue().equals(value)) {
            return DGPS;
        } else if (PPS.getValue().equals(value)) {
            return PPS;
        }
        return null;
    }

    public String toString() {
        return value;
    }
}
