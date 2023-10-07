/*
 * GPXConstants.java
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

package com.gpxmanager.gpx;

public final class GPXConstants {
    /*GPX nodes and attributes*/
    final static String GPX_NODE = "gpx";
    final static String WPT_NODE = "wpt";
    final static String TRK_NODE = "trk";
    final static String VERSION_ATTR = "version";
    final static String CREATOR_ATTR = "creator";
    /*End GPX nodes and attributes*/

    /*Waypoint nodes and attributes*/
    final static String LAT_ATTR = "lat";
    final static String LON_ATTR = "lon";
    final static String ELE_NODE = "ele";
    final static String TIME_NODE = "time";
    final static String NAME_NODE = "name";
    final static String CMT_NODE = "cmt";
    final static String DESC_NODE = "desc";
    final static String SRC_NODE = "src";
    final static String MAGVAR_NODE = "magvar";
    final static String GEOIDHEIGHT_NODE = "geoidheight";
    final static String LINK_NODE = "link";
    final static String SYM_NODE = "sym";
    final static String TYPE_NODE = "type";
    final static String FIX_NODE = "fix";
    final static String SAT_NODE = "sat";
    final static String HDOP_NODE = "hdop";
    final static String VDOP_NODE = "vdop";
    final static String PDOP_NODE = "pdop";
    final static String AGEOFGPSDATA_NODE = "ageofdgpsdata";
    final static String DGPSID_NODE = "dgpsid";
    final static String EXTENSIONS_NODE = "extensions";
    /*End Waypoint nodes and attributes*/

    /*Track nodes and attributes*/
    final static String NUMBER_NODE = "number";
    final static String TRKSEG_NODE = "trkseg";
    final static String TRKPT_NODE = "trkpt";
    /*End Track nodes and attributes*/

    /*Route Nodes*/
    final static String RTE_NODE = "rte";
    final static String RTEPT_NODE = "rtept";
    /*End route nodes*/

    /*Metadata*/
    final static String METADATA_NODE = "metadata";
    final static String METADATA_NAME = "name";
    final static String METADATA_DESCRIPTION = "desc";
    final static String METADATA_AUTHOR = "author";
    final static String METADATA_TIME = "time";
    final static String METADATA_KEYWORDS = "keywords";
    /*End Metadata*/
}
