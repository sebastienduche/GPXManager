package com.gpxmanager.strava;

public class GearItem {

    private final String id;
    private final String label;

    public GearItem(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
