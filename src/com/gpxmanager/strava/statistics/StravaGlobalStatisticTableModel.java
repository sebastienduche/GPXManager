package com.gpxmanager.strava.statistics;

import com.gpxmanager.Utils;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ACTIVITY;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ALTITUDE;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_DISTANCE;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_KM_100;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_KM_PER_DAY;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_PR;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_SPEED_MAX;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_TIME;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_YEAR;

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
    return StravaGlobalStatisticColumns.values()[columnIndex].getColumnClass();
  }

  public void setStatistics(List<StravaGlobalStatistic> statisticList) {
    SwingUtilities.invokeLater(() -> {
      this.statistics = statisticList;
      fireTableDataChanged();
    });
  }

  enum StravaGlobalStatisticColumns {
    COL_GLOBAL_YEAR("strava.table.year", Integer.class),
    COL_GLOBAL_ACTIVITY("strava.table.activities", Integer.class),
    COL_GLOBAL_DISTANCE("strava.table.distance", String.class),
    COL_GLOBAL_TIME("strava.table.time", Integer.class),
    COL_GLOBAL_SPEED_MAX("strava.table.max", Double.class),
    COL_GLOBAL_ALTITUDE("strava.table.altitude", String.class),
    COL_GLOBAL_PR("strava.table.pr", Integer.class),
    COL_GLOBAL_KM_PER_DAY("strava.table.km.day", String.class),
    COL_GLOBAL_KM_100("strava.table.day.100", Integer.class);

    private final String label;
    private final Class<?> columnClass;

    StravaGlobalStatisticColumns(String label, Class<?> columnClass) {
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

  static class StravaGlobalStatisticTableColumnModel extends DefaultTableColumnModel {

    public StravaGlobalStatisticTableColumnModel() {
      super();
      TableColumn colYear = new TableColumn(COL_GLOBAL_YEAR.ordinal(), 50);
      colYear.setHeaderValue(Utils.getLabel(COL_GLOBAL_YEAR.getLabel()));
      this.addColumn(colYear);
      TableColumn colActivity = new TableColumn(COL_GLOBAL_ACTIVITY.ordinal(), 100);
      colActivity.setHeaderValue(Utils.getLabel(COL_GLOBAL_ACTIVITY.getLabel()));
      this.addColumn(colActivity);
      TableColumn colDistance = new TableColumn(COL_GLOBAL_DISTANCE.ordinal(), 100);
      colDistance.setHeaderValue(Utils.getLabel(COL_GLOBAL_DISTANCE.getLabel()));
      this.addColumn(colDistance);
      TableColumn colTime = new TableColumn(COL_GLOBAL_TIME.ordinal(), 100, new DurationCellRenderer(), null);
      colTime.setHeaderValue(Utils.getLabel(COL_GLOBAL_TIME.getLabel()));
      this.addColumn(colTime);
      TableColumn colSpeedMax = new TableColumn(COL_GLOBAL_SPEED_MAX.ordinal(), 100, new MeterPerSecondToKmHCellRenderer(), null);
      colSpeedMax.setHeaderValue(Utils.getLabel(COL_GLOBAL_SPEED_MAX.getLabel()));
      this.addColumn(colSpeedMax);
      TableColumn colAltitude = new TableColumn(COL_GLOBAL_ALTITUDE.ordinal(), 100);
      colAltitude.setHeaderValue(Utils.getLabel(COL_GLOBAL_ALTITUDE.getLabel()));
      this.addColumn(colAltitude);
      TableColumn colPr = new TableColumn(COL_GLOBAL_PR.ordinal(), 50);
      colPr.setHeaderValue(Utils.getLabel(COL_GLOBAL_PR.getLabel()));
      this.addColumn(colPr);
      TableColumn colPerDay = new TableColumn(COL_GLOBAL_KM_PER_DAY.ordinal(), 60);
      colPerDay.setHeaderValue(Utils.getLabel(COL_GLOBAL_KM_PER_DAY.getLabel()));
      this.addColumn(colPerDay);
      TableColumn colHundred = new TableColumn(COL_GLOBAL_KM_100.ordinal(), 60);
      colHundred.setHeaderValue(Utils.getLabel(COL_GLOBAL_KM_100.getLabel()));
      this.addColumn(colHundred);
    }
  }
}
