package com.gpxmanager.strava.statistics;

public record StravaGlobalStatistic(int year, int activityCount, String distance, Integer time, double maxSpeed,
                                    double altitude, int prCount, String kmPerDay, int daysOverHundred) {

}
