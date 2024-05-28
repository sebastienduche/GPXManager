package com.gpxmanager.strava.statistics;

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
    return statistics == null ? 0 : statistics.size();
  }

  @Override
  public Object getValueAt(int row, int column) {
    Activity statistic = statistics.get(row);
    switch (StravaLongestRideStatisticColumns.values()[column]) {
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
    return switch (StravaLongestRideStatisticColumns.values()[columnIndex]) {
      case COL_LONGEST_DISTANCE, COL_LONGEST_AVG_SPEED, COL_LONGEST_TIME, COL_LONGEST_SPEED_MAX -> Double.class;
      case COL_LONGEST_DATE -> String.class;
      case COL_LONGEST_ALTITUDE -> Integer.class;
    };
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

  enum StravaLongestRideStatisticColumns {
    COL_LONGEST_DATE,
    COL_LONGEST_DISTANCE,
    COL_LONGEST_TIME,
    COL_LONGEST_AVG_SPEED,
    COL_LONGEST_SPEED_MAX,
    COL_LONGEST_ALTITUDE,
  }
}
