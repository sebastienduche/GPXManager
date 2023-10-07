package com.gpxmanager;

import com.gpxmanager.gpx.beans.Waypoint;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public record MyTime(long time, int hours, int minutes, int seconds) {

    public static final int SECONDS_IN_HOUR = 3600;

    public MyTime() {
        this(0, 0, 0, 0);
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

    public static MyTime add(MyTime time1, MyTime time2) {
        if (time1 == null || time2 == null) {
            return null;
        }
        long time = time1.time() + time2.time();
        return fromSeconds(time);
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
