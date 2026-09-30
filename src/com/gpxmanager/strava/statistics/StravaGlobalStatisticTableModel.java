package com.gpxmanager.strava.statistics;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class StravaGlobalStatisticTableModel extends DefaultTableModel {

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
    return StravaGlobalStatisticColumns.values().length;
  }

  @Override
  public String getColumnName(int column) {
    return getLabel(StravaGlobalStatisticColumns.values()[column].getLabel());
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
      case COL_GLOBAL_KM_100 -> statistic.daysOverHundred();
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (StravaGlobalStatisticColumns.values()[columnIndex]) {
      case COL_GLOBAL_SPEED_MAX -> Double.class;
      case COL_GLOBAL_DISTANCE, COL_GLOBAL_ALTITUDE, COL_GLOBAL_KM_PER_DAY -> String.class;
      case COL_GLOBAL_PR, COL_GLOBAL_YEAR, COL_GLOBAL_TIME, COL_GLOBAL_ACTIVITY, COL_GLOBAL_KM_100 -> Integer.class;
    };
  }

  public void setStatistics(List<StravaGlobalStatistic> statisticList) {
    SwingUtilities.invokeLater(() -> {
      this.statistics = statisticList;
      fireTableDataChanged();
    });
  }

  enum StravaGlobalStatisticColumns {
    COL_GLOBAL_YEAR("strava.table.year"),
    COL_GLOBAL_ACTIVITY("strava.table.activities"),
    COL_GLOBAL_DISTANCE("strava.table.distance"),
    COL_GLOBAL_TIME("strava.table.time"),
    COL_GLOBAL_SPEED_MAX("strava.table.max"),
    COL_GLOBAL_ALTITUDE("strava.table.altitude"),
    COL_GLOBAL_PR("strava.table.pr"),
    COL_GLOBAL_KM_PER_DAY("strava.table.km.day"),
    COL_GLOBAL_KM_100("strava.table.day.100");

    private final String label;

    StravaGlobalStatisticColumns(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }
}
