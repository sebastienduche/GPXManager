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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.gpxmanager.MyGPXManager.GSON;
import static com.gpxmanager.MyGPXManager.getInstance;
import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.strava.StravaTableModel.COL_ALTITUDE;
import static com.gpxmanager.strava.StravaTableModel.COL_DISTANCE;
import static com.gpxmanager.strava.StravaTableModel.COL_DOWNLOAD;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_AVG;
import static com.gpxmanager.strava.StravaTableModel.COL_SPEED_MAX;
import static com.gpxmanager.strava.StravaTableModel.COL_TIME;
import static com.gpxmanager.strava.StravaTableModel.COL_VIEW;

public class StravaPanel extends JPanel implements ITabListener {

    private final StravaConnection stravaConnection;
    private JTable table;
    private StravaTableModel stravaTableModel;
    private JButton downloadAllActivities = new JButton(new DownloadActivitiesAction());
    private JButton downloadNewActivities = new JButton(new DownloadNewActivitiesAction());
    private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();

    private final JLabel labelCount = new JLabel();


    public StravaPanel(StravaConnection stravaConnection, List<Activity> activities) {
        this.stravaConnection = stravaConnection;
        setLayout(new MigLayout("", "[grow]", "[][grow]10px[][]"));
        SwingUtilities.invokeLater(() -> {
            stravaTableModel = new StravaTableModel(stravaConnection);
            setActivities(activities);
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
            add(downloadAllActivities, "split 2");
            add(downloadNewActivities, "gapleft 10px, wrap");
            add(new JScrollPane(table), "grow, wrap");
            add(labelCount, "alignright, wrap");
            add(infoLabel, "center");
            downloadNewActivities.setEnabled(existStravaFile());
        });

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
                List<Activity> currentAthleteActivities = stravaConnection.getStrava().getCurrentAthleteActivitiesAll();
                setActivities(currentAthleteActivities);
                String json = GSON.toJson(currentAthleteActivities);
                try {
                    FileWriter fileWriter = new FileWriter(filePanel.getFile());
                    fileWriter.write(json);
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                infoLabel.setText("", true);
                setCursor(Cursor.getDefaultCursor());
            });
        }
    }

    private void setActivities(List<Activity> activities) {
        stravaTableModel.setActivities(activities);
        labelCount.setText(MessageFormat.format(getLabel("strava.activities.count"), activities.size()));
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
        try (FileReader fileReader = new FileReader(existingFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String json = bufferedReader.lines().reduce(String::concat).orElseThrow();
            infoLabel.setText(getLabel("download"), false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingUtilities.invokeLater(() -> {
                List<Activity> activities = new ArrayList<>(List.of(GSON.fromJson(json, Activity[].class)));
                Long maxId = activities.stream().map(Activity::getId).max(Long::compareTo).orElseThrow();
                List<Activity> newActivities = new ArrayList<>();
                List<Activity> collect;
                int page = 1;
                do {
                    List<Activity> currentAthleteActivities = stravaConnection.getStrava().getCurrentAthleteActivities(page, 100);
                    collect = currentAthleteActivities.stream().filter(activity -> activity.getId() > maxId).toList();
                    newActivities.addAll(collect);
                    page++;
                } while (!collect.isEmpty());
                activities.addAll(newActivities);
                activities = activities.stream().sorted(Comparator.comparingLong(Activity::getId).reversed()).toList();
                setActivities(activities);
                try {
                    FileWriter fileWriter = new FileWriter(existingFile);
                    fileWriter.write(GSON.toJson(activities));
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                infoLabel.setText(MessageFormat.format(getLabel("strava.countNew"), newActivities.size()), true);
                setCursor(Cursor.getDefaultCursor());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
