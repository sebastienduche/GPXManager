package com.gpxmanager.strava.statistics;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.Utils;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.entities.Activity;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gpxmanager.Utils.METER_IN_KM;
import static com.gpxmanager.Utils.getDaysOverHundred;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getTotalDistance;
import static com.gpxmanager.Utils.roundValue;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ACTIVITY;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ALTITUDE;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_DISTANCE;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_KM_100;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_KM_PER_DAY;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_PR;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_SPEED_MAX;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_TIME;
import static com.gpxmanager.strava.statistics.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_YEAR;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_ALTITUDE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_AVG_SPEED;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DATE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DISTANCE;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_SPEED_MAX;
import static com.gpxmanager.strava.statistics.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_TIME;
import static java.util.stream.Collectors.groupingBy;

public class StravaStatisticPanel extends JPanel implements ITabListener {

  private final JLabel labelCount = new JLabel();
  private final JLabel labelKm = new JLabel();
  private final JLabel labelCommute = new JLabel();
  private JTable tableGlobal;
  private JTable tableLongestRide;
  private StravaGlobalStatisticTableModel stravaGlobalStatisticTableModel;
  private StravaLongestRideStatisticTableModel stravaLongestRideStatisticTableModel;


  public StravaStatisticPanel(List<Activity> activities) {
    setLayout(new MigLayout("", "[grow]", "[][grow]0px"));
    JPanel panelGlobal = new JPanel();
    panelGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.global")));
    panelGlobal.setLayout(new MigLayout("", "[]", "[]"));
    panelGlobal.add(labelCount, " split 3, gapright 100px");
    panelGlobal.add(labelKm, "gapright 100px");
    panelGlobal.add(labelCommute, "gapright 100px");
    add(panelGlobal, "span 2, growx, wrap");
    SwingUtilities.invokeLater(() -> {
      Map<Integer, List<Activity>> activitiesPerYear = activities
          .stream()
          .collect(groupingBy(Utils::getStartYear));
      stravaGlobalStatisticTableModel = new StravaGlobalStatisticTableModel();
      stravaLongestRideStatisticTableModel = new StravaLongestRideStatisticTableModel();
      List<StravaGlobalStatistic> statisticList = new ArrayList<>();
      activitiesPerYear.keySet()
          .forEach(year -> {
            List<Activity> activitiesYear = activitiesPerYear.get(year);
            double totalDistance = getTotalDistance(activitiesYear);
            int daysOverHundred = getDaysOverHundred(activitiesYear);
            statisticList.add(new StravaGlobalStatistic(
                year,
                activitiesYear.size(),
                roundValue(totalDistance),
                activitiesYear.stream().map(Activity::getMovingTime).reduce(0, Integer::sum),
                activitiesYear.stream().mapToDouble(Activity::getMaxSpeed).max().orElse(0),
                activitiesYear.stream().map(Activity::getTotalElevationGain).reduce(0.0, Double::sum),
                activitiesYear.stream().map(Activity::getPrCount).reduce(0, Integer::sum),
                roundValue(totalDistance / getNbDaysPassed(year)),
                daysOverHundred
            ));
          });
      List<Activity> longestRidesList = activities.stream()
          .filter(activity -> activity.getDistance() > 100 * METER_IN_KM)
          .sorted(Comparator.comparing(Activity::getDistance).reversed())
          .collect(Collectors.toList());
      buildGlobalStatisticsTable(statisticList);
      buildLongestRideStatisticsTable(longestRidesList);
      setStatisticsPerYear(activities);
      JPanel panelTableGlobal = new JPanel();
      panelTableGlobal.setLayout(new MigLayout("", "0px[800:800:800]0px", "[grow]0px"));
      panelTableGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.per.year")));
      panelTableGlobal.add(new JScrollPane(tableGlobal), "grow");
      JPanel panelTableLongest = new JPanel();
      panelTableLongest.setLayout(new MigLayout("", "0px[800:800:800]0px", "[grow]0px"));
      panelTableLongest.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.per.distance")));
      panelTableLongest.add(new JScrollPane(tableLongestRide), "grow");
      add(panelTableGlobal, "wrap");
      add(panelTableLongest, "wrap");
    });
  }

  private static int getNbDaysPassed(Integer year) {
    if (LocalDate.now().getYear() == year) {
      return LocalDate.now().getDayOfYear();
    }
    return LocalDate.of(year, 1, 1).isLeapYear() ? 366 : 365;
  }

  private void buildGlobalStatisticsTable(List<StravaGlobalStatistic> statisticList) {
    stravaGlobalStatisticTableModel.setStatistics(statisticList);
    tableGlobal = new JTable(stravaGlobalStatisticTableModel);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_SPEED_MAX.ordinal()).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_TIME.ordinal()).setCellRenderer(new DurationCellRenderer());
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_YEAR.ordinal()).setMinWidth(50);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_YEAR.ordinal()).setMaxWidth(50);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_ACTIVITY.ordinal()).setMinWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_ACTIVITY.ordinal()).setMaxWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_PR.ordinal()).setMinWidth(50);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_PR.ordinal()).setMaxWidth(50);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_SPEED_MAX.ordinal()).setMinWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_SPEED_MAX.ordinal()).setMaxWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_TIME.ordinal()).setMinWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_TIME.ordinal()).setMaxWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_ALTITUDE.ordinal()).setMinWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_ALTITUDE.ordinal()).setMaxWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_DISTANCE.ordinal()).setMinWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_DISTANCE.ordinal()).setMaxWidth(100);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_KM_PER_DAY.ordinal()).setMinWidth(60);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_KM_PER_DAY.ordinal()).setMaxWidth(60);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_KM_100.ordinal()).setMinWidth(60);
    tableGlobal.getColumnModel().getColumn(COL_GLOBAL_KM_100.ordinal()).setMaxWidth(60);
  }

  private void buildLongestRideStatisticsTable(List<Activity> statisticList) {
    stravaLongestRideStatisticTableModel.setStatistics(statisticList);
    tableLongestRide = new JTable(stravaLongestRideStatisticTableModel);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setCellRenderer(new DurationCellRenderer());
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setMinWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setMaxWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setMinWidth(150);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setMaxWidth(150);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setMinWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setMaxWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DATE.ordinal()).setMinWidth(200);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DATE.ordinal()).setMaxWidth(200);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DISTANCE.ordinal()).setMinWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DISTANCE.ordinal()).setMaxWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_ALTITUDE.ordinal()).setMinWidth(100);
    tableLongestRide.getColumnModel().getColumn(COL_LONGEST_ALTITUDE.ordinal()).setMaxWidth(100);
  }

  private void setStatisticsPerYear(List<Activity> activities) {
    labelCount.setText(MessageFormat.format(getLabel("strava.activities.count"), activities.size()));
    String totalDistance = roundValue(getTotalDistance(activities));
    labelKm.setText(MessageFormat.format(getLabel("strava.km"), totalDistance));
    long count = activities.stream().filter(Activity::isCommute).count();
    labelCommute.setText(MessageFormat.format(getLabel("strava.commute"), count));
  }

  @Override
  public boolean tabWillClose(TabEvent tabEvent) {
    return true;
  }

  @Override
  public void tabClosed() {
    MyGPXManager.updateTabbedPane();
  }
}
