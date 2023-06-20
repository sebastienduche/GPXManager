package com.gpxmanager.component;

import com.gpxmanager.ProgramPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.Utils.getLabel;

public class ConfigureStravaStoragePanel extends JPanel {

    private final JTextField filePath = new JTextField();

    public ConfigureStravaStoragePanel() {
        setLayout(new MigLayout("", "[][400:400:]", "[]20px[][][]"));
        add(new JLabel(getLabel("configure.strava.title")), "center, span 2, wrap");
        add(new JLabel(getLabel("configure.strava.file")), "");
        add(filePath, "growx, wrap");
        filePath.setText(ProgramPreferences.getPreference(STRAVA_ALL_DATA, ""));
    }

    public void save() {
        ProgramPreferences.setPreference(STRAVA_ALL_DATA, filePath.getText());
    }
}
