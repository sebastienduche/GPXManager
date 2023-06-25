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
import static com.gpxmanager.Utils.getPersonalRecords;
import static com.gpxmanager.strava.StravaPanel.downloadGPXActivityOnStrava;
import static com.gpxmanager.strava.StravaPanel.openActivityOnStrava;

public class StravaTableModel extends DefaultTableModel {

    public static final int COL_DATE = 0;
    public static final int COL_NAME = 1;
    public static final int COL_DISTANCE = 2;
    public static final int COL_TIME = 3;
    public static final int COL_SPEED_MAX = 4;
    public static final int COL_SPEED_AVG = 5;
    public static final int COL_ALTITUDE = 6;
    public static final int COL_PR = 7;
    public static final int COL_VIEW = 8;
    public static final int COL_DOWNLOAD = 9;


    private final List<String> columns = List.of(
            getLabel("strava.table.date"),
            getLabel("strava.table.name"),
            getLabel("strava.table.distance"),
            getLabel("strava.table.time"),
            getLabel("strava.table.max"),
            getLabel("strava.table.avg"),
            getLabel("strava.table.altitude"),
            getLabel("strava.table.pr"),
            "",
            ""
    );

    private List<Activity> activities;

    public StravaTableModel() {
        this.activities = new ArrayList<>();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == COL_DOWNLOAD || column == COL_VIEW;
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
        if (activities == null) {
            return 0;
        }
        return activities.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        Activity activity = activities.get(row);
        switch (column) {
            case COL_DATE: {
                try {
                    Date localDateTime = TIMESTAMP.parse(activity.getStartDateLocal());
                    return DATE_HOUR_MINUTE.format(localDateTime);
                } catch (ParseException e) {
                    return null;
                }
            }
            case COL_NAME: {
                return activity.getName();
            }
            case COL_DISTANCE: {
                return activity.getDistance() / 1000;
            }
            case COL_TIME: {
                return activity.getMovingTime();
            }
            case COL_SPEED_MAX: {
                return activity.getMaxSpeed();
            }
            case COL_SPEED_AVG: {
                return activity.getAverageSpeed();
            }
            case COL_ALTITUDE: {
                return (int) activity.getTotalElevationGain();
            }
            case COL_PR: {
                return getPersonalRecords(activity).size();
            }
            case COL_VIEW:
            case COL_DOWNLOAD: {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COL_DATE:
            case COL_NAME: {
                return String.class;
            }
            case COL_DISTANCE:
            case COL_TIME:
            case COL_SPEED_MAX:
            case COL_SPEED_AVG: {
                return Double.class;
            }
            case COL_PR:
            case COL_ALTITUDE: {
                return Integer.class;
            }
            case COL_VIEW:
            case COL_DOWNLOAD: {
                return Boolean.class;
            }
        }
        return Object.class;
    }


    @Override
    public void setValueAt(Object aValue, int row, int column) {
        Activity activity = activities.get(row);
        switch (column) {
            case COL_VIEW: {
                openActivityOnStrava(activity);
                break;
            }
            case COL_DOWNLOAD: {
                downloadGPXActivityOnStrava(activity);
                break;
            }
        }
    }

    public void setActivities(List<Activity> activities) {
        SwingUtilities.invokeLater(() -> {
            this.activities = activities;
            fireTableDataChanged();
        });
    }

    public Activity getActivityAt(int selectedRow) {
        return activities.get(selectedRow);
    }

    public void setActivityAt(int selectedRow, Activity activity) {
        SwingUtilities.invokeLater(() -> {
            activities.remove(selectedRow);
            activities.add(selectedRow, activity);
            fireTableRowsUpdated(selectedRow, selectedRow);
        });
    }
}
