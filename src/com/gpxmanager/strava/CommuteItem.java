package com.gpxmanager.strava;

public record CommuteItem(String label, Boolean value) {

    @Override
    public String toString() {
        return label;
    }
}
