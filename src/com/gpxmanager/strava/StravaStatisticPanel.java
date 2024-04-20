package com.gpxmanager.strava;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.Utils;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.entities.Activity;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.ItemEvent;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getTotalDistance;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ACTIVITY;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_ALTITUDE;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_DISTANCE;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_PR;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_SPEED_MAX;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_TIME;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.StravaGlobalStatisticColumns.COL_GLOBAL_YEAR;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_ALTITUDE;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_AVG_SPEED;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DATE;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_DISTANCE;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_SPEED_MAX;
import static com.gpxmanager.strava.StravaLongestRideStatisticTableModel.StravaLongestRideStatisticColumns.COL_LONGEST_TIME;
import static java.util.stream.Collectors.groupingBy;

public class StravaStatisticPanel extends JPanel implements ITabListener {

    private final List<Activity> activities;
    private JTable tableGlobal;
    private JTable tableLongestRide;
    private StravaGlobalStatisticTableModel stravaGlobalStatisticTableModel;
    private StravaLongestRideStatisticTableModel stravaLongestRideStatisticTableModel;

    private final JLabel labelCount = new JLabel();
    private final JLabel labelKm = new JLabel();
    private final JLabel labelCommute = new JLabel();
    StravaChartPanel stravaChartPanel = new StravaChartPanel();
    private final JComboBox<GraphType> graphTypeCombo = new JComboBox<>();

    private enum GraphType {
        DISTANCE_PER_MONTH(getLabel("strava.statistics.graph.distance.month")),
        DISTANCE_PROGRESS(getLabel("strava.statistics.graph.distance.progress")),
        DISTANCE_PROGRESS_TODAY(getLabel("strava.statistics.graph.distance.progress.today")),
        ;
        private final String label;

      GraphType(String label) {
        this.label = label;
      }

        @Override
        public String toString() {
            return label;
        }
    }

    public StravaStatisticPanel(List<Activity> activities) {
        this.activities = activities;
        setLayout(new MigLayout("", "[grow][grow]", "[][200:200:200][grow]0px"));
        JPanel panelGlobal = new JPanel();
        panelGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.global")));
        panelGlobal.setLayout(new MigLayout("", "[]", "[]"));
        panelGlobal.add(labelCount, " split 3, gapright 100px");
        panelGlobal.add(labelKm, "gapright 100px");
        panelGlobal.add(labelCommute, "gapright 100px");
        panelGlobal.add(graphTypeCombo);
        add(panelGlobal, "span 2, growx, wrap");
        SwingUtilities.invokeLater(() -> {
            Arrays.stream(GraphType.values()).forEach(graphTypeCombo::addItem);
            Map<Integer, List<Activity>> activitiesPerYear = activities
                    .stream()
                    .collect(groupingBy(Utils::getStartYear));
            stravaGlobalStatisticTableModel = new StravaGlobalStatisticTableModel();
            stravaLongestRideStatisticTableModel = new StravaLongestRideStatisticTableModel();
            List<StravaGlobalStatistic> statisticList = new ArrayList<>();
            activitiesPerYear.keySet()
                    .forEach(year -> {
                        List<Activity> activitiesYear = activitiesPerYear.get(year);
                        String totalDistance = getTotalDistance(activitiesYear);
                        statisticList.add(new StravaGlobalStatistic(
                                year,
                                activitiesYear.size(),
                                totalDistance,
                                activitiesYear.stream().map(Activity::getMovingTime).reduce(0, Integer::sum),
                                activitiesYear.stream().mapToDouble(Activity::getMaxSpeed).max().orElse(0),
                                activitiesYear.stream().map(Activity::getTotalElevationGain).reduce(0.0, Double::sum),
                                activitiesYear.stream().map(Activity::getPrCount).reduce(0, Integer::sum)

                        ));
                    });
            List<Activity> longestRidesList = activities.stream()
                    .sorted(Comparator.comparing(Activity::getDistance).reversed())
                    .collect(Collectors.toList()).subList(0, 10);
            buildGlobalStatisticsTable(statisticList);
            buildLongestRideStatisticsTable(longestRidesList);
            setStatisticsPerYear(activities);
            JPanel panelTableGlobal = new JPanel();
            panelTableGlobal.setLayout(new MigLayout("", "0px[610:610:610]0px", "[grow]0px"));
            panelTableGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.per.year")));
            panelTableGlobal.add(new JScrollPane(tableGlobal), "grow");
            JPanel panelTableLongest = new JPanel();
            panelTableLongest.setLayout(new MigLayout("", "0px[610:610:610]0px", "[grow]0px"));
            panelTableLongest.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.per.distance")));
            panelTableLongest.add(new JScrollPane(tableLongestRide), "grow");
            add(panelTableGlobal, "split 2");
            add(panelTableLongest, "wrap");
            List<StatData> stats = buildStatsKmPerYear(activitiesPerYear);
            stravaChartPanel.setDataBarChart(stats, "");
            add(stravaChartPanel, "grow");

            graphTypeCombo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    GraphType item = (GraphType) e.getItem();
                    if (GraphType.DISTANCE_PER_MONTH.equals(item)) {
                        createDistancePerYearGraph();
                    } else if (GraphType.DISTANCE_PROGRESS.equals(item)) {
                        createDistanceProgressGraph(LocalDate.now().withMonth(12).withDayOfMonth(31));
                    }else if (GraphType.DISTANCE_PROGRESS_TODAY.equals(item)) {
                        createDistanceProgressGraph(LocalDate.now());
                    }
                }
            });
        });
    }

    private void createDistanceProgressGraph(LocalDate endDate) {
        Map<Integer, List<Activity>> activitiesPerYear = activities
            .stream()
            .collect(groupingBy(Utils::getStartYear));
        List<StatXYData> stats = buildStatsKmProgressPerDay(activitiesPerYear, endDate);
        stravaChartPanel.setXYLineChart(stats, "");
    }

    private void createDistancePerYearGraph() {
        Map<Integer, List<Activity>> activitiesPerYear = activities
            .stream()
            .collect(groupingBy(Utils::getStartYear));
        List<StatData> stats = buildStatsKmPerYear(activitiesPerYear);
        stravaChartPanel.setDataBarChart(stats, "");
    }

    private List<StatData> buildStatsKmPerYear(Map<Integer, List<Activity>> activitiesPerYear) {
        List<StatData> stats = new ArrayList<>();
        for (Integer year : activitiesPerYear.keySet()) {
            List<Activity> activitiesThisYear = activitiesPerYear.get(year);
            Map<Integer, List<Activity>> activityPerMonth = activitiesThisYear
                .stream()
                .collect(groupingBy(Utils::getStartMonth));
            for (Integer month : activityPerMonth.keySet()) {
                Double sum = activityPerMonth.get(month)
                    .stream()
                    .map(Activity::getDistance)
                    .reduce(0.0, Double::sum);
                stats.add(new StatData(sum / 1000, String.valueOf(year), Month.of(month+1).getDisplayName(TextStyle.SHORT, Utils.getLocale())));
            }
        }
        return stats;
    }

    private List<StatXYData> buildStatsKmProgressPerDay(Map<Integer, List<Activity>> activitiesPerYear, LocalDate endDate) {
        List<StatXYData> stats = new ArrayList<>();
        int endDateDayOfYear = endDate.getDayOfYear();
        for (Integer year : activitiesPerYear.keySet()) {
            List<Activity> activitiesThisYear = activitiesPerYear.get(year);
            Map<Integer, List<Activity>> activityPerDay = activitiesThisYear
                .stream()
                .collect(groupingBy(Utils::getStartDay));
            double totalDistance = 0;
            for (Integer day : activityPerDay.keySet()) {
                if (day > endDateDayOfYear) {
                    continue;
                }
                Double sum = activityPerDay.get(day)
                    .stream()
                    .map(Activity::getDistance)
                    .reduce(0.0, Double::sum);
                totalDistance += sum;
                stats.add(new StatXYData(String.valueOf(year), day, totalDistance / 1000));
            }
        }
        return stats;
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
    }

    private void buildLongestRideStatisticsTable(List<Activity> statisticList) {
        stravaLongestRideStatisticTableModel.setStatistics(statisticList);
        tableLongestRide = new JTable(stravaLongestRideStatisticTableModel);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setCellRenderer(new DurationCellRenderer());
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setMinWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_SPEED_MAX.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setMinWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_AVG_SPEED.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setMinWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_TIME.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DATE.ordinal()).setMinWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DATE.ordinal()).setMinWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DISTANCE.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_DISTANCE.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_ALTITUDE.ordinal()).setMaxWidth(100);
        tableLongestRide.getColumnModel().getColumn(COL_LONGEST_ALTITUDE.ordinal()).setMaxWidth(100);
    }

    private void setStatisticsPerYear(List<Activity> activities) {
        labelCount.setText(MessageFormat.format(getLabel("strava.activities.count"), activities.size()));
        String totalDistance = getTotalDistance(activities);
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
