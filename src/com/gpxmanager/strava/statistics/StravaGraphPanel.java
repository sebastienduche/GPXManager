package com.gpxmanager.strava.statistics;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.Utils;
import com.gpxmanager.strava.StatData;
import com.gpxmanager.strava.StravaChartPanel;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.entities.Activity;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getStartYear;
import static java.util.stream.Collectors.groupingBy;

public class StravaGraphPanel extends JPanel implements ITabListener {

    private final List<Activity> activities;

    StravaChartPanel stravaChartPanel = new StravaChartPanel();
    private final JComboBox<GraphType> graphTypeCombo = new JComboBox<>();
    private final JComboBox<GraphCompare> graphCompareCombo = new JComboBox<>();

    private final int currentYear = LocalDate.now().getYear();

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

    public StravaGraphPanel(List<Activity> activities) {
        this.activities = activities;
        setLayout(new MigLayout("", "grow", "[][grow]0px"));
        JPanel panelGlobal = new JPanel();
        panelGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ""));
        panelGlobal.setLayout(new MigLayout("", "[]", "[]"));
        panelGlobal.add(graphTypeCombo);
        panelGlobal.add(new JLabel(getLabel("strava.statistics.compare")));
        panelGlobal.add(graphCompareCombo);
        add(panelGlobal, "span 2, growx, wrap");
        SwingUtilities.invokeLater(() -> {
            Arrays.stream(GraphType.values()).forEach(graphTypeCombo::addItem);
            graphCompareCombo.addItem(new GraphCompare(-1, getLabel("strava.statistics.compare.all")));
            Map<Integer, List<Activity>> activitiesPerYear = activities
                    .stream()
                    .collect(groupingBy(Utils::getStartYear));
            activitiesPerYear.keySet()
                    .forEach(year -> graphCompareCombo.addItem(new GraphCompare(year, Integer.toString(year))));
            List<StatData> stats = buildStatsKmPerYear(activitiesPerYear);
            stravaChartPanel.setDataBarChart(stats, "");
            add(stravaChartPanel, "grow");

            graphTypeCombo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    GraphType item = (GraphType) e.getItem();
                    GraphCompare graphCompare = (GraphCompare) graphCompareCombo.getSelectedItem();
                  switch (item) {
                    case DISTANCE_PER_MONTH -> createDistancePerYearGraph(graphCompare.value());
                    case DISTANCE_PROGRESS ->
                        createDistanceProgressGraph(LocalDate.now().withMonth(12).withDayOfMonth(31), graphCompare.value());
                    case DISTANCE_PROGRESS_TODAY -> createDistanceProgressGraph(LocalDate.now(), graphCompare.value());
                  }
                }
            });

            graphCompareCombo.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    GraphCompare graphCompare = (GraphCompare) e.getItem();
                    GraphType item = (GraphType) graphTypeCombo.getSelectedItem();
                    switch (item) {
                        case DISTANCE_PER_MONTH -> createDistancePerYearGraph(graphCompare.value());
                        case DISTANCE_PROGRESS ->
                            createDistanceProgressGraph(LocalDate.now().withMonth(12).withDayOfMonth(31), graphCompare.value());
                        case DISTANCE_PROGRESS_TODAY -> createDistanceProgressGraph(LocalDate.now(), graphCompare.value());
                    }
                }
            });
        });
    }

    private void createDistanceProgressGraph(LocalDate endDate, int year) {
        Map<Integer, List<Activity>> activitiesPerYear = activities
            .stream()
            .filter(validYears(year))
            .collect(groupingBy(Utils::getStartYear));
        List<StatXYData> stats = buildStatsKmProgressPerDay(activitiesPerYear, endDate);
        stravaChartPanel.setXYLineChart(stats, "");
    }

    private void createDistancePerYearGraph(int year) {
        Map<Integer, List<Activity>> activitiesPerYear = activities
            .stream()
            .filter(validYears(year))
            .collect(groupingBy(Utils::getStartYear));
        List<StatData> stats = buildStatsKmPerYear(activitiesPerYear);
        stravaChartPanel.setDataBarChart(stats, "");
    }

    private Predicate<Activity> validYears(int year) {
      if (year == -1) {
          return activity -> true;
      }
      return activity -> {
            int startYear = getStartYear(activity);
            return startYear == year || startYear == currentYear;
        };
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
                stats.add(new StatData(sum / 1000, String.valueOf(year), Month.of(month + 1).getDisplayName(TextStyle.SHORT, Utils.getLocale())));
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

    @Override
    public boolean tabWillClose(TabEvent tabEvent) {
        return true;
    }

    @Override
    public void tabClosed() {
        MyGPXManager.updateTabbedPane();
    }
}
