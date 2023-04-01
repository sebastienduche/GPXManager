package com.gpxmanager.strava;

import org.jstrava.entities.Activity;

import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.gpxmanager.Utils.DATE_HOUR_MINUTE;
import static com.gpxmanager.Utils.TIMESTAMP;
import static com.gpxmanager.Utils.getLabel;

public class StravaTableModel extends DefaultTableModel {

    private static final int COL_DATE = 0;
    private static final int COL_NAME = 1;
    private static final int COL_DISTANCE = 2;

    private final List<String> columns = List.of(getLabel("strava.table.date"), getLabel("strava.table.name"), getLabel("strava.table.distance"));

    private final List<Activity> activities;

    public StravaTableModel(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

//    @Override
//    public void setValueAt(Object aValue, int row, int column) {
//        super.setValueAt(aValue, row, column);
//    }

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
            case COL_DATE -> {
                try {
                    Date localDateTime = TIMESTAMP.parse(activity.getStartDate());
                    return DATE_HOUR_MINUTE.format(localDateTime);
                } catch (ParseException e) {
                    return null;
                }
            }
            case COL_NAME -> {
                return activity.getName();
            }
            case COL_DISTANCE -> {
                NumberFormat numberInstance = NumberFormat.getNumberInstance();
                numberInstance.setMaximumFractionDigits(2);
                return numberInstance.format(activity.getDistance() / 1000);
            }
        }
        return null;
    }
}
