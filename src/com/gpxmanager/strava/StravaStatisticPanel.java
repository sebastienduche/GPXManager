package com.gpxmanager.strava;

import com.gpxmanager.MyGPXManager;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gpxmanager.Utils.TIMESTAMP;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getTotalDistance;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_ACTIVITY;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_DISTANCE;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_PR;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_SPEED_MAX;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_TIME;
import static com.gpxmanager.strava.StravaGlobalStatisticTableModel.COL_YEAR;
import static com.gpxmanager.strava.StravaTableModel.COL_ALTITUDE;
import static java.util.stream.Collectors.groupingBy;

public class StravaStatisticPanel extends JPanel implements ITabListener {

    private JTable table;
    private StravaGlobalStatisticTableModel stravaGlobalStatisticTableModel;

    private final JLabel labelCount = new JLabel();
    private final JLabel labelKm = new JLabel();

    public StravaStatisticPanel(List<Activity> activities) {
        setLayout(new MigLayout("", "[grow][grow]", "[][200:200:200]"));
        JPanel panelGlobal = new JPanel();
        panelGlobal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("strava.statistics.global")));
        panelGlobal.setLayout(new MigLayout("", "[]", "[]"));
        panelGlobal.add(labelCount, " split 2, gapright 100px");
        panelGlobal.add(labelKm, "wrap");
        add(panelGlobal, "span 2, growx, wrap");
        SwingUtilities.invokeLater(() -> {
            Map<Integer, List<Activity>> activitiesPerYear = activities
                    .stream()
                    .collect(groupingBy(this::getStartYear));
            stravaGlobalStatisticTableModel = new StravaGlobalStatisticTableModel();
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
                                activitiesYear.stream().map(Activity::getTotalElevationGain).reduce((double) 0, Double::sum),
                                activitiesYear.stream().map(Activity::getPrCount).reduce(0, Integer::sum)

                        ));
                    });
            setStatistics(activities, statisticList);
            table = new JTable(stravaGlobalStatisticTableModel);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_TIME).setCellRenderer(new DurationCellRenderer());
            table.getColumnModel().getColumn(COL_YEAR).setMinWidth(50);
            table.getColumnModel().getColumn(COL_YEAR).setMaxWidth(50);
            table.getColumnModel().getColumn(COL_ACTIVITY).setMinWidth(100);
            table.getColumnModel().getColumn(COL_ACTIVITY).setMaxWidth(100);
            table.getColumnModel().getColumn(COL_PR).setMinWidth(50);
            table.getColumnModel().getColumn(COL_PR).setMaxWidth(50);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setMinWidth(100);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setMaxWidth(100);
            table.getColumnModel().getColumn(COL_TIME).setMinWidth(100);
            table.getColumnModel().getColumn(COL_TIME).setMaxWidth(100);
            table.getColumnModel().getColumn(COL_ALTITUDE).setMinWidth(100);
            table.getColumnModel().getColumn(COL_ALTITUDE).setMaxWidth(100);
            table.getColumnModel().getColumn(COL_DISTANCE).setMinWidth(100);
            table.getColumnModel().getColumn(COL_DISTANCE).setMaxWidth(100);
            add(new JScrollPane(table), "w 610");
        });
    }

    private int getStartYear(Activity activity) {
        try {
            return TIMESTAMP.parse(activity.getStartDateLocal()).getYear() + 1900;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    private void setStatistics(List<Activity> activities, List<StravaGlobalStatistic> statisticList) {
        stravaGlobalStatisticTableModel.setStatistics(statisticList);
        labelCount.setText(MessageFormat.format(getLabel("strava.activities.count"), activities.size()));
        String totalDistance = getTotalDistance(activities);
        labelKm.setText(MessageFormat.format(getLabel("strava.km"), totalDistance));
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
