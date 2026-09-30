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
import static com.gpxmanager.strava.StravaPanel.openActivityOnStrava;

public class StravaLongestRideStatisticTableModel extends DefaultTableModel {

  private List<Activity> statistics;

  public StravaLongestRideStatisticTableModel() {
    this.statistics = new ArrayList<>();
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return StravaLongestRideStatisticColumns.values()[column] == StravaLongestRideStatisticColumns.COL_LONGEST_VIEW;
  }

  @Override
  public int getColumnCount() {
    return StravaLongestRideStatisticColumns.values().length;
  }

  @Override
  public String getColumnName(int column) {
    return getLabel(StravaLongestRideStatisticColumns.values()[column].getLabel());
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
      case COL_LONGEST_VIEW:
        return false;
    }
    return null;
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    Activity activity = statistics.get(row);
    switch (StravaLongestRideStatisticColumns.values()[column]) {
      case COL_LONGEST_VIEW -> openActivityOnStrava(activity);
      default ->
          throw new IllegalStateException("Can't set a value for column: " + StravaLongestRideStatisticColumns.values()[column]);
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (StravaLongestRideStatisticColumns.values()[columnIndex]) {
      case COL_LONGEST_DISTANCE, COL_LONGEST_AVG_SPEED, COL_LONGEST_TIME, COL_LONGEST_SPEED_MAX -> Double.class;
      case COL_LONGEST_DATE -> String.class;
      case COL_LONGEST_ALTITUDE -> Integer.class;
      case COL_LONGEST_VIEW -> Boolean.class;
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
    COL_LONGEST_DATE("strava.table.year"),
    COL_LONGEST_DISTANCE("strava.table.distance"),
    COL_LONGEST_TIME("strava.table.time"),
    COL_LONGEST_AVG_SPEED("strava.table.avg"),
    COL_LONGEST_SPEED_MAX("strava.table.max"),
    COL_LONGEST_ALTITUDE("strava.table.altitude"),
    COL_LONGEST_VIEW("");

    private final String label;

    StravaLongestRideStatisticColumns(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }
}
