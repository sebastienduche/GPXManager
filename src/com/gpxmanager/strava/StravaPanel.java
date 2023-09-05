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
import org.jstrava.entities.Activity;
import org.jstrava.entities.Gear;
import org.jstrava.exception.StravaException;
import org.jstrava.exception.StravaRequestException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
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
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import static com.gpxmanager.MyGPXManager.getMyTabbedPane;
import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static com.gpxmanager.Utils.METER_IN_KM;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.roundValue;
import static com.gpxmanager.Utils.writeToFile;
import static com.gpxmanager.strava.StravaTableModel.COL_ALTITUDE;
import static com.gpxmanager.strava.StravaTableModel.COL_DATE;
import static com.gpxmanager.strava.StravaTableModel.COL_DISTANCE;
import static com.gpxmanager.strava.StravaTableModel.COL_DOWNLOAD;
import static com.gpxmanager.strava.StravaTableModel.COL_NAME;
import static com.gpxmanager.strava.StravaTableModel.COL_PR;
import static com.gpxmanager.strava.StravaTableModel.COL_REFRESH;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_AVG;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_MAX;
import static com.gpxmanager.strava.StravaTableModel.COL_TIME;
import static com.gpxmanager.strava.StravaTableModel.COL_VIEW;
import static java.util.Comparator.comparingLong;

public class StravaPanel extends JPanel implements ITabListener {

    private final StravaConnection stravaConnection;
    private List<Activity> activities;

    private List<Gear> gears;
    private JTable table;
    private StravaTableModel stravaTableModel;
    private final JButton downloadAllActivities = new JButton(new DownloadActivitiesAction());
    private final JButton downloadNewActivities = new JButton(new DownloadNewActivitiesAction());
    private final JButton downloadActivity = new JButton(new DownloadActivityAction());
    private final JButton showStatistics = new JButton(new ShowStatisticsAction());
    private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();

    private final JLabel labelCount = new JLabel();
    private final JLabel labelKm = new JLabel();
    private final JTextField searchTextField = new JTextField();
    private final JSpinner minDistanceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JSpinner maxDistanceSpinner = new JSpinner(new SpinnerNumberModel(1000, 0, 1000, 1));

    private final JComboBox<GearItem> comboGear = new JComboBox<>();
    private final JComboBox<CommuteItem> comboCommute = new JComboBox<>();
    private String filter;
    private int minDistance = 0;
    private int maxDistance = 1000;
    private GearItem selectedGear;
    private CommuteItem selectedCommute;

    private static StravaPanel stravaPanel;


    public StravaPanel(StravaConnection stravaConnection, List<Activity> activities) {
        stravaPanel = this;
        this.stravaConnection = stravaConnection;
        this.activities = activities;
        searchTextField.setToolTipText(getLabel("strava.search"));
        setLayout(new MigLayout("", "[grow]", "[][][grow]10px[][]"));
        SwingUtilities.invokeLater(() -> {
            stravaTableModel = new StravaTableModel();
            setActivities(activities);
            gears = enrichWithGear(activities);
            populateGearCombo();
            populateCommuteCombo();
            table = new JTable(stravaTableModel);
            infoLabel.setForeground(Color.red);
            table.getColumnModel().getColumn(COL_TIME).setCellRenderer(new DurationCellRenderer());
            table.getColumnModel().getColumn(COL_DISTANCE).setCellRenderer(new RoundDoubleCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_MAX).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_SPEED_AVG).setCellRenderer(new MeterPerSecondToKmHCellRenderer());
            table.getColumnModel().getColumn(COL_VIEW).setCellRenderer(new ButtonCellRenderer("", MyGPXManagerImage.STRAVA, getLabel("strava.view")));
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellRenderer(new ButtonCellRenderer("", MyGPXManagerImage.SAVE, getLabel("strava.download")));
            table.getColumnModel().getColumn(COL_REFRESH).setCellRenderer(new ButtonCellRenderer("", MyGPXManagerImage.REFRESH, getLabel("strava.updateActivity")));
            table.getColumnModel().getColumn(COL_VIEW).setCellEditor(new ButtonCellEditor());
            table.getColumnModel().getColumn(COL_DOWNLOAD).setCellEditor(new ButtonCellEditor());
            table.getColumnModel().getColumn(COL_REFRESH).setCellEditor(new ButtonCellEditor());
            table.getColumnModel().getColumn(COL_VIEW).setMinWidth(25);
            table.getColumnModel().getColumn(COL_VIEW).setMaxWidth(25);
            table.getColumnModel().getColumn(COL_REFRESH).setMinWidth(25);
            table.getColumnModel().getColumn(COL_REFRESH).setMaxWidth(25);
            table.getColumnModel().getColumn(COL_PR).setMinWidth(25);
            table.getColumnModel().getColumn(COL_PR).setMaxWidth(25);
            table.getColumnModel().getColumn(COL_DOWNLOAD).setMinWidth(25);
            table.getColumnModel().getColumn(COL_DOWNLOAD).setMaxWidth(25);
            table.getColumnModel().getColumn(COL_DISTANCE).setMinWidth(50);
            table.getColumnModel().getColumn(COL_DISTANCE).setPreferredWidth(100);
            table.getColumnModel().getColumn(COL_DISTANCE).setMaxWidth(100);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setMinWidth(50);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setPreferredWidth(150);
            table.getColumnModel().getColumn(COL_SPEED_MAX).setMaxWidth(200);
            table.getColumnModel().getColumn(COL_SPEED_AVG).setMinWidth(50);
            table.getColumnModel().getColumn(COL_SPEED_AVG).setPreferredWidth(150);
            table.getColumnModel().getColumn(COL_ALTITUDE).setMinWidth(50);
            table.getColumnModel().getColumn(COL_ALTITUDE).setPreferredWidth(1500);
            table.getColumnModel().getColumn(COL_ALTITUDE).setMaxWidth(200);
            table.getColumnModel().getColumn(COL_TIME).setMinWidth(50);
            table.getColumnModel().getColumn(COL_TIME).setPreferredWidth(150);
            table.getColumnModel().getColumn(COL_TIME).setMaxWidth(200);
            table.getColumnModel().getColumn(COL_DATE).setMinWidth(50);
            table.getColumnModel().getColumn(COL_DATE).setPreferredWidth(150);
            table.getColumnModel().getColumn(COL_DATE).setMaxWidth(200);
            table.getColumnModel().getColumn(COL_NAME).setMinWidth(300);
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            table.getColumnModel().getColumn(COL_ALTITUDE).setCellRenderer(leftRenderer);
            table.setAutoCreateRowSorter(true);
            JPopupMenu popup = new JPopupMenu();
            popup.add(new JMenuItem(new OpenInStravaAction()));
            popup.add(new JMenuItem(new DownloadFromStravaAction()));
            popup.add(new JMenuItem(new UpdateActivityFromStravaAction()));
            popup.add(new JMenuItem(new ShowJSONAction()));
            table.setComponentPopupMenu(popup);
            add(downloadAllActivities, "split 4");
            add(downloadNewActivities, "gapleft 10px");
            add(downloadActivity, "gapleft 10px");
            add(showStatistics, "gapleft 10px, wrap");
            add(new JLabel(), "split 8, growx");
            add(comboGear);
            add(comboCommute);
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
                    filterActivities(extractText(e), minDistance, maxDistance, selectedGear, selectedCommute);
                }
            });
            minDistanceSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                filterActivities(filter, (int) spinner.getValue(), maxDistance, selectedGear, selectedCommute);
            });
            maxDistanceSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                filterActivities(filter, minDistance, (int) spinner.getValue(), selectedGear, selectedCommute);
            });
            comboGear.addItemListener(e -> filterActivities(filter, minDistance, maxDistance, (GearItem) e.getItem(), selectedCommute));
            comboCommute.addItemListener(e -> filterActivities(filter, minDistance, maxDistance, selectedGear, (CommuteItem) e.getItem()));
        });

    }

    public static void openActivityOnStrava(Activity activity) {
        try {
            String activityAsGPXURL = stravaPanel.stravaConnection.getStrava().getActivityAsGPX(activity.getId());
            Desktop.getDesktop().browse(URI.create(activityAsGPXURL.substring(0, activityAsGPXURL.lastIndexOf('/'))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadGPXActivityOnStrava(Activity activity) {
        try {
            Desktop.getDesktop().browse(URI.create(stravaPanel.stravaConnection.getStrava().getActivityAsGPX(activity.getId())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateGearCombo() {
        comboGear.removeAllItems();
        comboGear.addItem(new GearItem("", getLabel("strava.all.bikes")));
        gears.forEach(gear -> comboGear.addItem(new GearItem(gear.getId(), gear.getName())));
    }

    private void populateCommuteCombo() {
        comboCommute.removeAllItems();
        comboCommute.addItem(new CommuteItem(getLabel("strava.all.type"), null));
        comboCommute.addItem(new CommuteItem(getLabel("strava.commute.yes"), true));
        comboCommute.addItem(new CommuteItem(getLabel("strava.commute.no"), false));
    }

    private static String extractText(DocumentEvent e) {
        try {
            return e.getDocument().getText(0, e.getDocument().getLength());
        } catch (BadLocationException ignored) {
        }
        return null;
    }

    private void filterActivities(String value, int min, int max, GearItem gear, CommuteItem item) {
        if (value == null || value.isBlank()) {
            filter = "";
        } else {
            filter = value;
        }
        minDistance = min;
        maxDistance = max;
        selectedGear = gear;
        selectedCommute = item;
        setActivities(activities
                .stream()
                .filter(this::filterActivity)
                .collect(Collectors.toList()));
    }

    private boolean filterActivity(Activity activity) {
        return activity.getDistance() >= (minDistance * METER_IN_KM) &&
                activity.getDistance() <= (maxDistance * METER_IN_KM) &&
                (selectedGear == null || selectedGear.getId().isBlank() || selectedGear.getId().equals(activity.getGearId())) &&
                (selectedCommute == null || selectedCommute.getValue() == null || selectedCommute.getValue().equals(activity.isCommute())) &&
                (filter.isBlank() || activity.getName().toLowerCase().contains(filter.toLowerCase()) ||
                        Long.toString(activity.getId()).equals(filter));
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
            super(getLabel("strava.downloadAll"), MyGPXManagerImage.DOWNLOAD_SEVERAL);
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
                    manageStravaException(ex, null);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            });
        }
    }

    private static <T> T manageStravaException(StravaException ex, Class<T> classOfT) {
        if (ex instanceof StravaRequestException && ((StravaRequestException) ex).getHttpStatusCode() == 404) {
            return null; // Managed when needed
        }
        if (ex instanceof StravaRequestException && ((StravaRequestException) ex).getHttpStatusCode() == 401) {
            stravaPanel.stravaConnection.refreshToken();
            if (classOfT != null) {
                return stravaPanel.stravaConnection.getStrava().retryGet(classOfT);
            }
            JOptionPane.showMessageDialog(getInstance(),
                    getLabel("strava.noConnection"), getLabel("information"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        throw new RuntimeException(ex);
    }

    private void setActivities(List<Activity> activities) {
        activities.sort(Comparator.comparing(Activity::getId).reversed());
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
            super(getLabel("strava.downloadNew"), MyGPXManagerImage.DOWNLOAD);
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

    class DownloadActivityAction extends AbstractAction {

        public DownloadActivityAction() {
            super(getLabel("strava.downloadActivity"), null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String value = JOptionPane.showInputDialog(getInstance(), getLabel("strava.activityQuestion"));
            try {
                long id = Long.parseLong(value);
                infoLabel.setText(getLabel("download"), false);
                Activity activityFromStrava = findActivityFromStrava(id);
                performActivityAction(activityFromStrava);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(getInstance(),
                        getLabel("strava.errorNotNumeric"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            } catch (StravaException ex) {
                Activity activity = manageStravaException(ex, Activity.class);
                if (activity != null) {
                    performActivityAction(activity);
                }
                if (ex instanceof StravaRequestException && ((StravaRequestException) ex).getHttpStatusCode() == 404) {
                    JOptionPane.showMessageDialog(getInstance(),
                            getLabel("strava.notFound"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void performActivityAction(Activity activityFromStrava) {
            Activity foundActivity = activities
                    .stream()
                    .filter(activity -> activityFromStrava.getId() == activity.getId())
                    .findFirst().orElse(null);
            infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), 1), true);
            if (foundActivity != null) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(stravaPanel, getLabel("strava.alreadyExist"), getLabel("information"), JOptionPane.YES_NO_OPTION)) {
                    activities.remove(foundActivity);
                    activities.add(activityFromStrava);
                    setActivities(activities);
                    save();
                }
            } else {
                activities.add(activityFromStrava);
                setActivities(activities);
                save();
            }
        }
    }

    class ShowStatisticsAction extends AbstractAction {

        public ShowStatisticsAction() {
            super(getLabel("strava.statistics"), MyGPXManagerImage.STATS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getMyTabbedPane().addTab(getLabel("strava.statistics"), MyGPXManagerImage.STATS, new StravaStatisticPanel(activities), true);
        }
    }

    private void downloadLatestActivities(String existingFile) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        infoLabel.setText(getLabel("download"), false);
        SwingUtilities.invokeLater(() -> {
            Long maxId = activities.stream().map(Activity::getId).max(Long::compareTo).orElseThrow();
            List<Activity> newActivities = new ArrayList<>();
            List<Activity> collect = new ArrayList<>();
            int page = 1;
            do {
                List<Activity> currentAthleteActivities = null;
                try {
                    currentAthleteActivities = stravaConnection.getStrava().getCurrentAthleteActivities(page, 100);
                } catch (StravaException e) {
                    Activity[] elements = manageStravaException(e, Activity[].class);
                    if (elements != null) {
                        currentAthleteActivities = List.of(elements);
                    }
                }
                if (currentAthleteActivities != null) {
                    collect = currentAthleteActivities.stream().filter(activity -> activity.getId() > maxId).collect(Collectors.toList());
                    newActivities.addAll(collect);
                    page++;
                }
            } while (!collect.isEmpty());
            if (newActivities.isEmpty()) {
                infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), 0), true);
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            newActivities.forEach(activity -> {
                try {
                    activities.add(stravaConnection.getStrava().findActivity(activity.getId(), true));
                } catch (StravaException e) {
                    Activity activity1 = manageStravaException(e, Activity.class);
                    if (activity1 != null) {
                        activities.add(activity1);
                    }
                }
            });
            activities = activities.stream().sorted(comparingLong(Activity::getId).reversed()).collect(Collectors.toList());
            gears = enrichWithGear(activities);
            setActivities(activities);
            writeToFile(GSON.toJson(activities), new File(existingFile));
            populateGearCombo();
            infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), newActivities.size()), true);
            setCursor(Cursor.getDefaultCursor());
        });
    }

    class DownloadFromStravaAction extends AbstractAction {

        public DownloadFromStravaAction() {
            super(getLabel("strava.download"), MyGPXManagerImage.SAVE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Activity activity = stravaTableModel.getActivityAt(table.convertRowIndexToModel(table.getSelectedRow()));
            downloadGPXActivityOnStrava(activity);
        }
    }

    class OpenInStravaAction extends AbstractAction {

        public OpenInStravaAction() {
            super(getLabel("strava.view"), MyGPXManagerImage.STRAVA);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Activity activity = stravaTableModel.getActivityAt(table.convertRowIndexToModel(table.getSelectedRow()));
            openActivityOnStrava(activity);
        }
    }

    class UpdateActivityFromStravaAction extends AbstractAction {

        public UpdateActivityFromStravaAction() {
            super(getLabel("strava.updateActivity"), MyGPXManagerImage.REFRESH);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Activity activity = stravaTableModel.getActivityAt(table.convertRowIndexToModel(table.getSelectedRow()));
            updateActivityFromStrava(activity, table.getSelectedRow());
        }
    }

    class ShowJSONAction extends AbstractAction {

        public ShowJSONAction() {
            super(getLabel("strava.showJson"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Activity activity = stravaTableModel.getActivityAt(table.convertRowIndexToModel(table.getSelectedRow()));
            updateActivityFromStrava(activity, table.getSelectedRow());
            JPanel panel = new JPanel();
            panel.setLayout(new MigLayout("", "[500:500:500]", "[500:500:500]"));
            JTextArea textArea = new JTextArea(GSON.toJson(activity).replaceAll(",", ",\n"));
            panel.add(new JScrollPane(textArea));
            panel.setSize(500, 500);
            JOptionPane.showOptionDialog(null, panel, getLabel("strava.showJson"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        }
    }

    public static void updateActivityFromStrava(Activity oldActivity, int selectedRow) {
        try {
            Activity newActivity = stravaPanel.stravaConnection.getStrava().findActivity(oldActivity.getId(), true);
            updateActivity(oldActivity, selectedRow, newActivity);
        } catch (StravaException e) {
            Activity activity = manageStravaException(e, Activity.class);
            if (activity != null) {
                updateActivity(oldActivity, selectedRow, activity);
            }
            if (e instanceof StravaRequestException && ((StravaRequestException) e).getHttpStatusCode() == 404) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(stravaPanel, getLabel("strava.notFound.askDelete"), getLabel("information"), JOptionPane.YES_NO_OPTION)) {
                    removeActivityAtRow(oldActivity, selectedRow);
                }
            }
        }
    }

    private static void removeActivityAtRow(Activity oldActivity, int selectedRow) {
        stravaPanel.activities.remove(oldActivity);
        stravaPanel.stravaTableModel.fireTableRowsDeleted(selectedRow, selectedRow);
        save();
    }

    private static void updateActivity(Activity oldActivity, int selectedRow, Activity newActivity) {
        int i = stravaPanel.activities.indexOf(oldActivity);
        stravaPanel.activities.remove(oldActivity);
        stravaPanel.activities.add(i, newActivity);
        stravaPanel.stravaTableModel.setActivityAt(selectedRow, newActivity);
        stravaPanel.infoLabel.setText(getLabel("strava.updateActivity.done"), true);
        save();
    }

    public static Activity findActivityFromStrava(long id) throws StravaException {
        return stravaPanel.stravaConnection.getStrava().findActivity(id, true);

    }

    private static void save() {
        String existingFile = getPreference(STRAVA_ALL_DATA, null);
        if (existingFile != null && new File(existingFile).exists()) {
            writeToFile(GSON.toJson(stravaPanel.activities), new File(existingFile));
        }
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
                    Gear gear = manageStravaException(e, Gear.class);
                    if (gear != null) {
                        map.put(gearID, gear);
                    }
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
        return new ArrayList<>(map.values());
    }
}
