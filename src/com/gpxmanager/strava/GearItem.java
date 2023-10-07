package com.gpxmanager.strava;

public record GearItem(String id, String label) {

    @Override
    public String toString() {
        return label;
    }
}
