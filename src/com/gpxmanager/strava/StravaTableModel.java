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
import static com.gpxmanager.strava.StravaPanel.updateActivityFromStrava;
import static com.gpxmanager.strava.StravaTableModel.StravaTableColumn.COL_DOWNLOAD;
import static com.gpxmanager.strava.StravaTableModel.StravaTableColumn.COL_REFRESH;
import static com.gpxmanager.strava.StravaTableModel.StravaTableColumn.COL_VIEW;

public class StravaTableModel extends DefaultTableModel {

  private List<Activity> activities;

  public StravaTableModel() {
    this.activities = new ArrayList<>();
  }

  private static String formatStartDate(Activity activity) {
    try {
      Date localDateTime = TIMESTAMP.parse(activity.getStartDateLocal());
      return DATE_HOUR_MINUTE.format(localDateTime);
    } catch (ParseException e) {
      return null;
    }
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    StravaTableColumn value = StravaTableColumn.values()[column];
    return value == COL_DOWNLOAD
        || value == COL_VIEW
        || value == COL_REFRESH;
  }

  @Override
  public int getColumnCount() {
    return StravaTableColumn.values().length;
  }

  @Override
  public String getColumnName(int column) {
    return StravaTableColumn.values()[column].getKwyLabel();
  }

  @Override
  public int getRowCount() {
    return activities == null ? 0 : activities.size();
  }

  @Override
  public Object getValueAt(int row, int column) {
    Activity activity = activities.get(row);
    return switch (StravaTableColumn.values()[column]) {
      case COL_DATE -> formatStartDate(activity);
      case COL_NAME -> activity.getName();
      case COL_DISTANCE -> activity.getDistance() / 1000;
      case COL_TIME -> activity.getMovingTime();
      case COL_SPEED_MAX -> activity.getMaxSpeed();
      case COL_SPEED_AVG -> activity.getAverageSpeed();
      case COL_ALTITUDE -> (int) activity.getTotalElevationGain();
      case COL_PR -> getPersonalRecords(activity).size();
      case COL_VIEW, COL_REFRESH, COL_DOWNLOAD -> Boolean.FALSE;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (StravaTableColumn.values()[columnIndex]) {
      case COL_DATE, COL_NAME -> String.class;
      case COL_DISTANCE, COL_TIME, COL_SPEED_MAX, COL_SPEED_AVG -> Double.class;
      case COL_PR, COL_ALTITUDE -> Integer.class;
      case COL_VIEW, COL_REFRESH, COL_DOWNLOAD -> Boolean.class;
    };
  }

  @Override
  public void setValueAt(Object aValue, int row, int column) {
    Activity activity = activities.get(row);
    switch (StravaTableColumn.values()[column]) {
      case COL_VIEW -> openActivityOnStrava(activity);
      case COL_DOWNLOAD -> downloadGPXActivityOnStrava(activity);
      case COL_REFRESH -> updateActivityFromStrava(activity, row);
      default -> throw new IllegalStateException("Can't set a value for column: " + StravaTableColumn.values()[column]);
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

  enum StravaTableColumn {
    COL_DATE("strava.table.date"),
    COL_NAME("strava.table.name"),
    COL_DISTANCE("strava.table.distance"),
    COL_TIME("strava.table.time"),
    COL_SPEED_MAX("strava.table.max"),
    COL_SPEED_AVG("strava.table.avg"),
    COL_ALTITUDE("strava.table.altitude"),
    COL_PR("strava.table.pr"),
    COL_VIEW(""),
    COL_DOWNLOAD(""),
    COL_REFRESH("");

    private final String key;

    StravaTableColumn(String key) {
      this.key = key;
    }

    public String getKwyLabel() {
      return key.isEmpty() ? "" : getLabel(key);
    }
  }
}
