package com.gpxmanager.strava;

public record StravaGlobalStatistic(int year, int activityCount, String distance, Integer time, double maxSpeed,
                                    double altitude, int prCount) {

}
