package com.gpxmanager.strava;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class StravaGlobalStatisticTableModel extends DefaultTableModel {

    public static final int COL_GLOBAL_YEAR = 0;
    public static final int COL_GLOBAL_ACTIVITY = 1;
    public static final int COL_GLOBAL_DISTANCE = 2;
    public static final int COL_GLOBAL_TIME = 3;
    public static final int COL_GLOBAL_SPEED_MAX = 4;
    public static final int COL_GLOBAL_ALTITUDE = 5;
    public static final int COL_GLOBAL_PR = 6;

    private final List<String> columns = List.of(
            getLabel("strava.table.year"),
            getLabel("strava.table.activities"),
            getLabel("strava.table.distance"),
            getLabel("strava.table.time"),
            getLabel("strava.table.max"),
            getLabel("strava.table.altitude"),
            getLabel("strava.table.pr")
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
        if (statistics == null) {
            return 0;
        }
        return statistics.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        StravaGlobalStatistic statistic = statistics.get(row);
        switch (column) {
            case COL_GLOBAL_YEAR: {
                return statistic.getYear();
            }
            case COL_GLOBAL_ACTIVITY: {
                return statistic.getActivityCount();
            }
            case COL_GLOBAL_DISTANCE: {
                return statistic.getDistance();
            }
            case COL_GLOBAL_TIME: {
                return statistic.getTime();
            }
            case COL_GLOBAL_SPEED_MAX: {
                return statistic.getMaxSpeed();
            }
            case COL_GLOBAL_ALTITUDE: {
                return (int) statistic.getAltitude();
            }
            case COL_GLOBAL_PR: {
                return statistic.getPrCount();
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COL_GLOBAL_SPEED_MAX: {
                return Double.class;
            }
            case COL_GLOBAL_DISTANCE:
            case COL_GLOBAL_ALTITUDE: {
                return String.class;
            }
            case COL_GLOBAL_PR:
            case COL_GLOBAL_YEAR:
            case COL_GLOBAL_TIME:
            case COL_GLOBAL_ACTIVITY: {
                return Integer.class;
            }
        }
        return Object.class;
    }


    public void setStatistics(List<StravaGlobalStatistic> statisticList) {
        SwingUtilities.invokeLater(() -> {
            this.statistics = statisticList;
            fireTableDataChanged();
        });
    }

    public StravaGlobalStatistic getActivityAt(int selectedRow) {
        return statistics.get(selectedRow);
    }
}
