package com.gpxmanager.geocalc;

public class Degree {

    private final double longitude;
    private final double latitude;

    public Degree(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
