package com.gpxmanager.strava.statistics;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class StravaGlobalStatisticTableModel extends DefaultTableModel {

    enum StravaGlobalStatisticColumns {
        COL_GLOBAL_YEAR,
        COL_GLOBAL_ACTIVITY,
        COL_GLOBAL_DISTANCE,
        COL_GLOBAL_TIME,
        COL_GLOBAL_SPEED_MAX,
        COL_GLOBAL_ALTITUDE,
        COL_GLOBAL_PR,
        COL_GLOBAL_KM_PER_DAY
    }

    private final List<String> columns = List.of(
            getLabel("strava.table.year"),
            getLabel("strava.table.activities"),
            getLabel("strava.table.distance"),
            getLabel("strava.table.time"),
            getLabel("strava.table.max"),
            getLabel("strava.table.altitude"),
            getLabel("strava.table.pr"),
            getLabel("strava.table.km.day")
    );

    private List<StravaGlobalStatistic> statistics;

    public StravaGlobalStatisticTableModel() {
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
        return statistics == null ? 0 : statistics.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        StravaGlobalStatistic statistic = statistics.get(row);
        return switch (StravaGlobalStatisticColumns.values()[column]) {
            case COL_GLOBAL_YEAR -> statistic.year();
            case COL_GLOBAL_ACTIVITY -> statistic.activityCount();
            case COL_GLOBAL_DISTANCE -> statistic.distance();
            case COL_GLOBAL_TIME -> statistic.time();
            case COL_GLOBAL_SPEED_MAX -> statistic.maxSpeed();
            case COL_GLOBAL_ALTITUDE -> (int) statistic.altitude();
            case COL_GLOBAL_PR -> statistic.prCount();
            case COL_GLOBAL_KM_PER_DAY -> statistic.kmPerDay();
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (StravaGlobalStatisticColumns.values()[columnIndex]) {
            case COL_GLOBAL_SPEED_MAX -> Double.class;
            case COL_GLOBAL_DISTANCE, COL_GLOBAL_ALTITUDE, COL_GLOBAL_KM_PER_DAY -> String.class;
            case COL_GLOBAL_PR, COL_GLOBAL_YEAR, COL_GLOBAL_TIME, COL_GLOBAL_ACTIVITY -> Integer.class;
        };
    }


    public void setStatistics(List<StravaGlobalStatistic> statisticList) {
        SwingUtilities.invokeLater(() -> {
            this.statistics = statisticList;
            fireTableDataChanged();
        });
    }

}
