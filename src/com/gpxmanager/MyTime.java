package com.gpxmanager;

import com.gpxmanager.gpx.beans.Waypoint;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MyTime {

    public static final int SECONDS_IN_HOUR = 3600;
    private long time;
    private int hours;
    private int minutes;
    private int seconds;

    public MyTime() {
        time = 0;
        hours = minutes = seconds = 0;
    }

    public MyTime(long time, int hours, int minutes, int seconds) {
        this.time = time;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
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

    public int getSeconds() {
        return seconds;
    }

    public static MyTime getTrackTime(List<Waypoint> waypoints) {
        List<Date> times = waypoints
                .stream()
                .map(Waypoint::getTime)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        if (times.size() < 2) {
            return null;
        }
        Date startTime = times.get(0);
        Date endTime = times.get(times.size() - 1);
        long time = endTime.getTime() - startTime.getTime();
        return fromSeconds(time / 1000);
    }

    public void add(MyTime myTime) {
        if (myTime == null) {
            return;
        }
        time += myTime.getTime();
        MyTime newTime = fromSeconds(time);
        hours = newTime.getHours();
        minutes = newTime.getMinutes();
        seconds = newTime.getSeconds();
    }

    public static MyTime fromSeconds(long totalSeconds) {
        long diffMinutes = totalSeconds / 60;
        int hours = (int) (totalSeconds / SECONDS_IN_HOUR);
        int minutes = (int) (diffMinutes - (hours * 60));
        int seconds = (int) (totalSeconds - (diffMinutes * 60));
        return new MyTime(totalSeconds, hours, minutes, seconds);
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
        if (seconds < 10) {
            builder.append(0);
        }
        builder.append(seconds).append("s ");
        return builder.toString();
    }
}
