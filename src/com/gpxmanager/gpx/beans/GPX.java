/*
 * GPX.java
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

package com.gpxmanager.gpx.beans;

import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * This class holds com.gpxmanager.gpx information from a &lt;com.gpxmanager.gpx&gt; node.
 * <br>
 * <p>GPX specification for this tag:</p>
 * <code>
 * &lt;com.gpxmanager.gpx version="1.1" creator=""xsd:string [1]"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;metadata&gt; xsd:string &lt;/metadata&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;wpt&gt; xsd:string &lt;/wpt&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;rte&gt; xsd:string &lt;/rte&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;trk&gt; xsd:string &lt;/trk&gt; [0..1]<br>
 * &nbsp;&nbsp;&nbsp;&lt;extensions&gt; extensionsType &lt;/extensions&gt; [0..1]<br>
 * &lt;/com.gpxmanager.gpx&gt;<br>
 * </code>
 */
public class GPX extends Extension {


    private String version;
    private String creator;

    private final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    private HashSet<Waypoint> waypoints;
    private HashSet<Track> tracks;
    private HashSet<Route> routes;

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    private Metadata metadata;

    /**
     * Returns the version of a com.gpxmanager.gpx object
     *
     * @return A String representing the version of this com.gpxmanager.gpx object
     */
    public String getVersion() {
        return version;
    }

    /**
     * Setter for com.gpxmanager.gpx version property. This maps to <i>version</i> attribute value.
     *
     * @param version A String representing the version of a com.gpxmanager.gpx file.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the creator of this com.gpxmanager.gpx object
     *
     * @return A String representing the creator of a com.gpxmanager.gpx object
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Setter for com.gpxmanager.gpx creator property. This maps to <i>creator</i> attribute value.
     *
     * @param creator A String representing the creator of a com.gpxmanager.gpx file.
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Getter for the list of waypoints from a com.gpxmanager.gpx objecty
     *
     * @return a HashSet of {@link Waypoint}
     */
    public HashSet<Waypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * Setter for the list of waypoints from a com.gpxmanager.gpx object
     *
     * @param waypoints a HashSet of {@link Waypoint}
     */
    public void setWaypoints(HashSet<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    /**
     * Getter for the list of Tracks from a com.gpxmanager.gpx objecty
     *
     * @return a HashSet of {@link Track}
     */
    public HashSet<Track> getTracks() {
        return tracks;
    }

    /**
     * Setter for the list of tracks from a com.gpxmanager.gpx object
     *
     * @param tracks a HashSet of {@link Track}
     */
    public void setTracks(HashSet<Track> tracks) {
        this.tracks = tracks;
    }

    /**
     * Getter for the list of routes from a com.gpxmanager.gpx object
     *
     * @return a HashSet of {@link Route}
     */
    public HashSet<Route> getRoutes() {
        return routes;
    }

    /**
     * Setter for the list of routes from a com.gpxmanager.gpx object
     *
     * @param routes a HashSet of {@link Route}
     */
    public void setRoutes(HashSet<Route> routes) {
        this.routes = routes;
    }

    /**
     * Adds a new waypoint to a com.gpxmanager.gpx object
     *
     * @param waypoint a {@link Waypoint}
     */
    public void addWaypoint(Waypoint waypoint) {
        if (waypoints == null) {
            waypoints = new HashSet<>();
        }
        waypoints.add(waypoint);

    }

    /**
     * Adds a new track to a com.gpxmanager.gpx object
     *
     * @param track a {@link Track}
     */
    public void addTrack(Track track) {
        if (tracks == null) {
            tracks = new HashSet<>();
        }
        tracks.add(track);
    }

    /**
     * Adds a new Route to a com.gpxmanager.gpx object
     *
     * @param route a {@link Route}
     */
    public void addRoute(Route route) {
        if (routes == null) {
            routes = new HashSet<>();
        }
        routes.add(route);
    }
}
