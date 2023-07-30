package com.gpxmanager.strava;

public class StravaGlobalStatistic {

    private final int year;
    private final int activityCount;
    private final String distance;
    private final Integer time;
    private final double maxSpeed;
    private final double altitude;
    private final int prCount;

    public StravaGlobalStatistic(int year, int activityCount, String distance, Integer time, double maxSpeed, double altitude, int prCount) {
        this.year = year;
        this.activityCount = activityCount;
        this.distance = distance;
        this.time = time;
        this.maxSpeed = maxSpeed;
        this.altitude = altitude;
        this.prCount = prCount;
    }

    public int getYear() {
        return year;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public String getDistance() {
        return distance;
    }

    public Integer getTime() {
        return time;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAltitude() {
        return altitude;
    }

    public int getPrCount() {
        return prCount;
    }
}
