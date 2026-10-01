package com.gpxmanager.strava.statistics;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.Utils;
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
      stravaGlobalStatisticTableModel.setStatistics(statisticList);
      tableGlobal = new JTable(stravaGlobalStatisticTableModel, new StravaGlobalStatisticTableModel.StravaGlobalStatisticTableColumnModel());
      stravaLongestRideStatisticTableModel.setStatistics(longestRidesList);
      tableLongestRide = new JTable(stravaLongestRideStatisticTableModel, new StravaLongestRideStatisticTableModel.StravaLongestRideStatisticTableColumnModel());
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
