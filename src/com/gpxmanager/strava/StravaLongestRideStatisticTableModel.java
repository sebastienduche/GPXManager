package com.gpxmanager.strava;

import org.jstrava.entities.Activity;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gpxmanager.Utils.DATE_HOUR_MINUTE;
import static com.gpxmanager.Utils.TIMESTAMP;
import static com.gpxmanager.Utils.getLabel;

public class StravaLongestRideStatisticTableModel extends DefaultTableModel {

    public static final int COL_LONGEST_DATE = 0;
    public static final int COL_LONGEST_DISTANCE = 1;
    public static final int COL_LONGEST_TIME = 2;
    public static final int COL_LONGEST_AVG_SPEED = 3;
    public static final int COL_LONGEST_SPEED_MAX = 4;
    public static final int COL_LONGEST_ALTITUDE = 5;

    private final List<String> columns = List.of(
            getLabel("strava.table.year"),
            getLabel("strava.table.distance"),
            getLabel("strava.table.time"),
            getLabel("strava.table.avg"),
            getLabel("strava.table.max"),
            getLabel("strava.table.altitude")
    );

    private List<Activity> statistics;

    public StravaLongestRideStatisticTableModel() {
        this.statistics = new ArrayList<>();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }

    @Override
    public int getRowCount() {
        if (statistics == null) {
            return 0;
        }
        return statistics.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        Activity statistic = statistics.get(row);
        switch (column) {
            case COL_LONGEST_DATE: {
                try {
                    Date localDateTime = TIMESTAMP.parse(statistic.getStartDateLocal());
                    return DATE_HOUR_MINUTE.format(localDateTime);
                } catch (ParseException e) {
                    return null;
                }
            }
            case COL_LONGEST_AVG_SPEED: {
                return statistic.getAverageSpeed();
            }
            case COL_LONGEST_DISTANCE: {
                return statistic.getDistance() / 1000;
            }
            case COL_LONGEST_TIME: {
                return statistic.getMovingTime();
            }
            case COL_LONGEST_SPEED_MAX: {
                return statistic.getMaxSpeed();
            }
            case COL_LONGEST_ALTITUDE: {
                return (int) statistic.getTotalElevationGain();
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COL_LONGEST_DISTANCE:
            case COL_LONGEST_AVG_SPEED:
            case COL_LONGEST_TIME:
            case COL_LONGEST_SPEED_MAX: {
                return Double.class;
            }
            case COL_LONGEST_DATE: {
                return String.class;
            }
            case COL_LONGEST_ALTITUDE: {
                return Integer.class;
            }
        }
        return Object.class;
    }


    public void setStatistics(List<Activity> statisticList) {
        SwingUtilities.invokeLater(() -> {
            this.statistics = statisticList;
            fireTableDataChanged();
        });
    }

    public Activity getActivityAt(int selectedRow) {
        return statistics.get(selectedRow);
    }
}
