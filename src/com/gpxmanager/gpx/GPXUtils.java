package com.gpxmanager.gpx;

public class GPXUtils {

    private final static GPXParser GPX_PARSER = new GPXParser();

    public static GPXParser getGpxParser() {
        return GPX_PARSER;
    }
}
