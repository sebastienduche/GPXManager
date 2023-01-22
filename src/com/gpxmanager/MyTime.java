package com.gpxmanager;

import com.gpxmanager.gpx.beans.Waypoint;

import java.util.Date;
import java.util.List;

public class MyTime {

    private long time;
    private int hours;
    private int minutes;
    private int secondes;

    public MyTime() {
        time = 0;
        hours = minutes = secondes = 0;
    }

    public MyTime(long time, int hours, int minutes, int secondes) {
        this.time = time;
        this.hours = hours;
        this.minutes = minutes;
        this.secondes = secondes;
    }

    public long getTime() {
        return time;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSecondes() {
        return secondes;
    }

    public static MyTime getTrackTime(List<Waypoint> waypoints) {
        List<Date> times = waypoints
                .stream()
                .map(Waypoint::getTime)
                .sorted()
                .toList();
        if (times.size() < 2) {
            return null;
        }
        Date startTime = times.get(0);
        Date endTime = times.get(times.size() - 1);
        long time = endTime.getTime() - startTime.getTime();
        time /= 1000;
        long diffMinutes = time / 60;
        int hours = (int) (time / (60 * 60));
        int minutes = (int) (diffMinutes - (hours * 60));
        int secondes = (int) (time - (diffMinutes * 60));

        return new MyTime(time, hours, minutes, secondes);
    }

    public void add(MyTime myTime) {
        if (myTime == null) {
            return;
        }
        time += myTime.getTime();
        long diffMinutes = time / 60;
        hours = (int) (time / (60 * 60));
        minutes = (int) (diffMinutes - (hours * 60));
        secondes = (int) (time - (diffMinutes * 60));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes < 10) {
            builder.append(0);
        }
        builder.append(minutes).append("m ");
        if (secondes < 10) {
            builder.append(0);
        }
        builder.append(secondes).append("s ");
        return builder.toString();
    }
}
