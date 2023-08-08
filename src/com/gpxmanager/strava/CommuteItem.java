package com.gpxmanager.strava;

public class CommuteItem {

    private final String label;
    private final Boolean value;

    public CommuteItem(String label, Boolean value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public Boolean getValue() {
        return value;
    }
}
