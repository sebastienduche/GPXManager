package com.gpxmanager.strava;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.component.FilePanel;
import com.gpxmanager.component.renderer.ButtonCellEditor;
import com.gpxmanager.component.renderer.ButtonCellRenderer;
import com.gpxmanager.component.renderer.DurationCellRenderer;
import com.gpxmanager.component.renderer.MeterPerSecondToKmHCellRenderer;
import com.gpxmanager.component.renderer.RoundDoubleCellRenderer;
import com.mycomponents.MyAutoHideLabel;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;
import org.jstrava.StravaConnection;
import org.jstrava.StravaException;
import org.jstrava.entities.Activity;
import org.jstrava.entities.Gear;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.gpxmanager.MyGPXManager.GSON;
import static com.gpxmanager.MyGPXManager.getInstance;
import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static com.gpxmanager.Utils.METER_IN_KM;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.roundValue;
import static com.gpxmanager.Utils.writeToFile;
import static com.gpxmanager.strava.StravaTableModel.COL_ALTITUDE;
import static com.gpxmanager.strava.StravaTableModel.COL_DISTANCE;
import static com.gpxmanager.strava.StravaTableModel.COL_DOWNLOAD;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_AVG;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_MAX;
import static com.gpxmanager.strava.StravaTableModel.COL_TIME;
import static com.gpxmanager.strava.StravaTableModel.COL_VIEW;

public class StravaPanel extends JPanel implements ITabListener {

    private final StravaConnection stravaConnection;
    private List<Activity> activities;

    private List<Gear> gears;
    private JTable table;
    private StravaTableModel stravaTableModel;
    private JButton downloadAllActivities = new JButton(new DownloadActivitiesAction());
    private JButton downloadNewActivities = new JButton(new DownloadNewActivitiesAction());
    private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();

    private final JLabel labelCount = new JLabel();
    private final JLabel labelKm = new JLabel();
    private final JTextField searchTextField = new JTextField();
    private final JSpinner minDistanceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner maxDistanceSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, 1000, 1));

    private final JComboBox<GearItem> comboGear = new JComboBox<>();
    private String filter;
    private int minDistance = 0;
    private int maxDistance = 1000;
    private GearItem selectedGear;


    public StravaPanel(StravaConnection stravaConnection, List<Activity> activities) {
        this.stravaConnection = stravaConnection;
        this.activities = activities;
        searchTextField.setToolTipText(getLabel("strava.search"));
        setLayout(new MigLayout("", "[grow]", "[][grow]10px[][]"));
        SwingUtilities.invokeLater(() -> {
            stravaTableModel = new StravaTableModel(stravaConnection);
            setActivities(activities);
            gears = enrichWithGear(activities);
            populateGearCombo();
            table = new JTable(stravaTableModel);
            infoLabel.setForeground(Color.red);
            table.getColumnModel().getColumn(COL_TIME).setCellRenderer(new DurationCellRenderer());
            table.getColumnModel().getColumn(COL_DISTANCE).setCellRenderer(new RoundDoubleCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_MAX).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_AVG).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_VIEW).setCellRenderer(new ButtonCellRenderer(getLabel("strava.view")));
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellRenderer(new ButtonCellRenderer(getLabel("strava.download")));
            table.getColumnModel().getColumn(COL_VIEW).setCellEditor(new ButtonCellEditor());
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellEditor(new ButtonCellEditor());
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            table.getColumnModel().getColumn(COL_ALTITUDE).setCellRenderer(leftRenderer);
            table.setAutoCreateRowSorter(true);
            add(downloadAllActivities, "split 9");
            add(downloadNewActivities, "gapleft 10px");
            add(new JLabel(), "growx");
            add(comboGear);
            add(new JLabel(getLabel("filter.fromDistance")));
            add(minDistanceSpinner, "w 50, align right");
            add(new JLabel(getLabel("filter.toDistance")));
            add(maxDistanceSpinner, "w 50, align right");
            add(searchTextField, "w 200, align right, wrap");
            add(new JScrollPane(table), "grow, wrap");
            add(labelKm, "split 2, growx");
            add(labelCount, "alignright, wrap");
            add(infoLabel, "center");
            downloadNewActivities.setEnabled(existStravaFile());
            searchTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performTextChange(e);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    performTextChange(e);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    performTextChange(e);
                }

                private void performTextChange(DocumentEvent e) {
                    filterActivities(extractText(e), minDistance, maxDistance, selectedGear);
                }
            });
            minDistanceSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                filterActivities(filter, (int) spinner.getValue(), maxDistance, selectedGear);
            });
            maxDistanceSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                filterActivities(filter, minDistance, (int) spinner.getValue(), selectedGear);
            });
            comboGear.addItemListener(e -> filterActivities(filter, minDistance, maxDistance, (GearItem) e.getItem()));
        });

    }

    private void populateGearCombo() {
        comboGear.removeAllItems();
        comboGear.addItem(new GearItem("", getLabel("strava.all.bikes")));
        gears.forEach(gear -> comboGear.addItem(new GearItem(gear.getId(), gear.getName())));
    }

    private static String extractText(DocumentEvent e) {
        try {
            return e.getDocument().getText(0, e.getDocument().getLength());
        } catch (BadLocationException ignored) {
        }
        return null;
    }

    private void filterActivities(String value, int min, int max, GearItem gear) {
        if (value == null || value.isBlank()) {
            filter = "";
        } else {
            filter = value;
        }
        minDistance = min;
        maxDistance = max;
        selectedGear = gear;
        setActivities(activities
                .stream()
                .filter(this::filterActivity)
                .collect(Collectors.toList()));
    }

    private boolean filterActivity(Activity activity) {
        return activity.getDistance() >= (minDistance * METER_IN_KM) &&
                activity.getDistance() <= (maxDistance * METER_IN_KM) &&
                (selectedGear == null || selectedGear.getId().isBlank() || selectedGear.getId().equals(activity.getGearId())) &&
                (filter.isBlank() || activity.getName().toLowerCase().contains(filter.toLowerCase()));
    }

    private boolean existStravaFile() {
        String existingFile = getPreference(STRAVA_ALL_DATA, null);
        return existingFile != null && new File(existingFile).exists();
    }

    @Override
    public boolean tabWillClose(TabEvent tabEvent) {
        return true;
    }

    @Override
    public void tabClosed() {
        MyGPXManager.updateTabbedPane();
    }

    class DownloadActivitiesAction extends AbstractAction {

        public DownloadActivitiesAction() {
            super(getLabel("strava.downloadAll"), MyGPXManagerImage.IMPORT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String existingFile = getPreference(STRAVA_ALL_DATA, null);
            if (existingFile != null && new File(existingFile).exists()) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(getInstance(),
                        MessageFormat.format(getLabel("strava.fileExist"), existingFile), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    downloadLatestActivities(existingFile);
                    return;
                }
            }
            FilePanel filePanel = new FilePanel(FilePanel.Type.SAVE);
            JOptionPane.showMessageDialog(getInstance(), filePanel,
                    "",
                    JOptionPane.PLAIN_MESSAGE);
            if (filePanel.getFile() == null) {
                JOptionPane.showMessageDialog(getInstance(),
                        getLabel("strava.errorFile"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            setPreference(STRAVA_ALL_DATA, filePanel.getFile().getAbsolutePath());
            infoLabel.setText(getLabel("download"), false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingUtilities.invokeLater(() -> {
                List<Activity> currentAthleteActivities;
                try {
                    currentAthleteActivities = stravaConnection.getStrava().getCurrentAthleteActivitiesAll();
                    gears = enrichWithGear(currentAthleteActivities);
                    setActivities(currentAthleteActivities);
                    String json = GSON.toJson(currentAthleteActivities);
                    writeToFile(json, filePanel.getFile());
                    infoLabel.setText("", true);
                    populateGearCombo();
                } catch (StravaException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            });
        }
    }

    private void setActivities(List<Activity> activities) {
        stravaTableModel.setActivities(activities);
        labelCount.setText(MessageFormat.format(getLabel("strava.activities.count"), activities.size()));
        String totalDistance = roundValue(activities.stream()
                .map(Activity::getDistance)
                .reduce(Double::sum)
                .orElseGet(() -> (double) 0) / METER_IN_KM);
        labelKm.setText(MessageFormat.format(getLabel("strava.km"), totalDistance));
    }

    class DownloadNewActivitiesAction extends AbstractAction {

        public DownloadNewActivitiesAction() {
            super(getLabel("strava.downloadNew"), MyGPXManagerImage.IMPORT);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String existingFile = getPreference(STRAVA_ALL_DATA, null);
            if (existingFile == null || !new File(existingFile).exists()) {
                JOptionPane.showMessageDialog(getInstance(),
                        getLabel("strava.errorNoFile"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            downloadLatestActivities(existingFile);
        }
    }

    private void downloadLatestActivities(String existingFile) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        infoLabel.setText(getLabel("download"), false);
        SwingUtilities.invokeLater(() -> {
            Long maxId = activities.stream().map(Activity::getId).max(Long::compareTo).orElseThrow();
            List<Activity> newActivities = new ArrayList<>();
            List<Activity> collect;
            int page = 1;
            do {
                List<Activity> currentAthleteActivities;
                try {
                    currentAthleteActivities = stravaConnection.getStrava().getCurrentAthleteActivities(page, 100);
                } catch (StravaException e) {
                    throw new RuntimeException(e);
                }
                collect = currentAthleteActivities.stream().filter(activity -> activity.getId() > maxId).toList();
                newActivities.addAll(collect);
                page++;
            } while (!collect.isEmpty());
            if (newActivities.isEmpty()) {
                infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), 0), true);
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            activities.addAll(newActivities);
            activities = activities.stream().sorted(Comparator.comparingLong(Activity::getId).reversed()).toList();
            gears = enrichWithGear(activities);
            setActivities(activities);
            writeToFile(GSON.toJson(activities), new File(existingFile));
            populateGearCombo();
            infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), newActivities.size()), true);
            setCursor(Cursor.getDefaultCursor());
        });
    }

    private List<Gear> enrichWithGear(List<Activity> activities) {
        List<String> gearIDs = activities
                .stream()
                .map(Activity::getGearId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Gear> map = new HashMap<>();
        activities
                .stream()
                .map(Activity::getGear)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(gear -> {
                    if (!map.containsKey(gear.getId())) {
                        map.put(gear.getId(), gear);
                    }
                });

        for (String gearID : gearIDs) {
            if (!map.containsKey(gearID)) {
                try {
                    Gear gear = stravaConnection.getStrava().findGear(gearID);
                    map.put(gearID, gear);
                } catch (StravaException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Activity activity : activities) {
            if (activity.getGear() == null) {
                Gear gear = map.get(activity.getGearId());
                if (gear != null) {
                    activity.setGear(gear);
                }
            }
        }
        return map.values().stream().toList();
    }
}
