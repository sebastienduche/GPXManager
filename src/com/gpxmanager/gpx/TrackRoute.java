package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;

import java.util.ArrayList;

public record TrackRoute(
        boolean isTrack,
        int index,
        String name,
        String comment,
        String description,
        String src,
        Integer number,
        String type,
        ArrayList<Waypoint> routePoints) {

    public TrackRoute(Track track, int index) {
        this(true, index, track.getName(), track.getComment(), track.getDescription(), track.getSrc(), track.getNumber(), track.getType(), track.getTrackPoints());
    }

    public TrackRoute(Route route, int index) {
        this(false, index, route.getName(), route.getComment(), route.getDescription(), route.getSrc(), route.getNumber(), route.getType(), route.getRoutePoints());
    }

}
