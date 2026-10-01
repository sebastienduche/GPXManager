package com.gpxmanager.strava.statistics;

import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.gpxmanager.component.renderer.ButtonCellEditor;
import com.gpxmanager.component.renderer.ButtonCellRenderer;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;
import org.jstrava.entities.Activity;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gpxmanager.Utils.DATE_HOUR_MINUTE;
import static com.gpxmanager.Utils.TIMESTAMP;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.strava.StravaPanel.openActivityOnStrava;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_ALTITUDE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_AVG_SPEED;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DATE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DISTANCE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_SPEED_MAX;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_TIME;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_VIEW;

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
    return StravaLongestRideStatisticColumns.values()[columnIndex].getColumnClass();
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
    COL_LONGEST_DATE("strava.table.year", String.class),
    COL_LONGEST_DISTANCE("strava.table.distance", Double.class),
    COL_LONGEST_TIME("strava.table.time", Double.class),
    COL_LONGEST_AVG_SPEED("strava.table.avg", Double.class),
    COL_LONGEST_SPEED_MAX("strava.table.max", Double.class),
    COL_LONGEST_ALTITUDE("strava.table.altitude", Integer.class),
    COL_LONGEST_VIEW("", Boolean.class);

    private final String label;
    private final Class<?> columnClass;

    StravaLongestRideStatisticColumns(String label, Class<?> columnClass) {
      this.label = label;
      this.columnClass = columnClass;
    }

    public String getLabel() {
      return this.label;
    }

    public Class<?> getColumnClass() {
      return this.columnClass;
    }
  }

  static class StravaLongestRideStatisticTableColumnModel extends DefaultTableColumnModel {

    public StravaLongestRideStatisticTableColumnModel() {
      super();
      var colDate = new TableColumn(COL_LONGEST_DATE.ordinal(), 200);
      colDate.setHeaderValue(Utils.getLabel(COL_LONGEST_DATE.getLabel()));
      this.addColumn(colDate);
      var colDistance = new TableColumn(COL_LONGEST_DISTANCE.ordinal(), 100);
      colDistance.setHeaderValue(Utils.getLabel(COL_LONGEST_DISTANCE.getLabel()));
      this.addColumn(colDistance);
      var colTime = new TableColumn(COL_LONGEST_TIME.ordinal(), 100, new DurationCellRenderer(), null);
      colTime.setHeaderValue(Utils.getLabel(COL_LONGEST_TIME.getLabel()));
      this.addColumn(colTime);
      var colAvg = new TableColumn(COL_LONGEST_AVG_SPEED.ordinal(), 150, new MeterPerSecondToKmHCellRenderer(), null);
      colAvg.setHeaderValue(Utils.getLabel(COL_LONGEST_AVG_SPEED.getLabel()));
      this.addColumn(colAvg);
      var colMax = new TableColumn(COL_LONGEST_SPEED_MAX.ordinal(), 100, new MeterPerSecondToKmHCellRenderer(), null);
      colMax.setHeaderValue(Utils.getLabel(COL_LONGEST_SPEED_MAX.getLabel()));
      this.addColumn(colMax);
      var colAltitude = new TableColumn(COL_LONGEST_ALTITUDE.ordinal(), 100);
      colAltitude.setHeaderValue(Utils.getLabel(COL_LONGEST_ALTITUDE.getLabel()));
      this.addColumn(colAltitude);
      TableColumn colView = new TableColumn(COL_LONGEST_VIEW.ordinal(), 25, new ButtonCellRenderer("", MyGPXManagerImage.STRAVA, getLabel("strava.view")), new ButtonCellEditor());
      colView.setHeaderValue(Utils.getLabel(COL_LONGEST_VIEW.getLabel()));
      this.addColumn(colView);
    }
  }
}
