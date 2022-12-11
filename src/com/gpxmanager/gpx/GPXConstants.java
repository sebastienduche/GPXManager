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

public interface GPXConstants {
	/*GPX nodes and attributes*/
	String GPX_NODE = "gpx";
	String WPT_NODE = "wpt";
	String TRK_NODE = "trk";
	String VERSION_ATTR = "version";
	String CREATOR_ATTR = "creator";
	/*End GPX nodes and attributes*/
	
	/*Waypoint nodes and attributes*/
	String LAT_ATTR = "lat";
	String LON_ATTR = "lon";
	String ELE_NODE = "ele";
	String TIME_NODE = "time";
	String NAME_NODE = "name";
	String CMT_NODE = "cmt";
	String DESC_NODE = "desc";
	String SRC_NODE = "src";
	String MAGVAR_NODE = "magvar";
	String GEOIDHEIGHT_NODE = "geoidheight";
	String LINK_NODE = "link";
	String SYM_NODE = "sym";
	String TYPE_NODE = "type";
	String FIX_NODE = "fix";
	String SAT_NODE = "sat";
	String HDOP_NODE = "hdop";
	String VDOP_NODE = "vdop";
	String PDOP_NODE = "pdop";
	String AGEOFGPSDATA_NODE = "ageofdgpsdata";
	String DGPSID_NODE = "dgpsid";
	String EXTENSIONS_NODE = "extensions";
	/*End Waypoint nodes and attributes*/
	
	/*Track nodes and attributes*/
	String NUMBER_NODE = "number";
	String TRKSEG_NODE = "trkseg";
	String TRKPT_NODE = "trkpt";
	/*End Track nodes and attributes*/
	
	/*Route Nodes*/
	String RTE_NODE = "rte";
	String RTEPT_NODE = "rtept";
	/*End route nodes*/

}
