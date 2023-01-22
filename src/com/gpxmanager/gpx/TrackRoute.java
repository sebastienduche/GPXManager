package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;

import java.util.ArrayList;

public class TrackRoute {

    private final boolean isTrack;
    private final int index;
    private final String name;
    private final String comment;
    private final String description;
    private final String src;
    private final Integer number;
    private final String type;
    private final ArrayList<Waypoint> routePoints;

    public TrackRoute(Track track, int index) {
        isTrack = true;
        this.index = index;
        name = track.getName();
        comment = track.getComment();
        description = track.getDescription();
        src = track.getSrc();
        number = track.getNumber();
        type = track.getType();
        routePoints = track.getTrackPoints();
    }

    public TrackRoute(Route route, int index) {
        isTrack = false;
        this.index = index;
        name = route.getName();
        comment = route.getComment();
        description = route.getDescription();
        src = route.getSrc();
        number = route.getNumber();
        type = route.getType();
        routePoints = route.getRoutePoints();
    }

    public boolean isTrack() {
        return isTrack;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getDescription() {
        return description;
    }

    public String getSrc() {
        return src;
    }

    public Integer getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Waypoint> getRoutePoints() {
        return routePoints;
    }
}
