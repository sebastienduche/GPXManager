package com.gpxmanager.component;

import com.gpxmanager.ProgramPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static com.gpxmanager.ProgramPreferences.GPS_MOUNT_DIR;
import static com.gpxmanager.ProgramPreferences.GPS_MOUNT_ROOT;
import static com.gpxmanager.ProgramPreferences.GPS_TARGET_DIR;
import static com.gpxmanager.Utils.getLabel;

public class ConfigureDevicePanel extends JPanel {

    private final JTextField root = new JTextField();
    private final JTextField pathDevice = new JTextField();
    private final JTextField targetDir = new JTextField();

    public ConfigureDevicePanel() {
        setLayout(new MigLayout("", "[][200:200:]", "[]20px[][][]"));
        add(new JLabel(getLabel("configure.device.title")), "center, span 2, wrap");
        add(new JLabel(getLabel("configure.device.root")), "");
        add(root, "growx, wrap");
        add(new JLabel(getLabel("configure.device.path")), "");
        add(pathDevice, "growx, wrap");
        add(new JLabel(getLabel("configure.device.target")), "");
        add(targetDir, "growx, wrap");
        root.setText(ProgramPreferences.getPreference(GPS_MOUNT_ROOT, ""));
        pathDevice.setText(ProgramPreferences.getPreference(GPS_MOUNT_DIR, ""));
        targetDir.setText(ProgramPreferences.getPreference(GPS_TARGET_DIR, ""));
    }

    public void save() {
        ProgramPreferences.setPreference(GPS_MOUNT_ROOT, root.getText());
        ProgramPreferences.setPreference(GPS_MOUNT_DIR, pathDevice.getText());
        ProgramPreferences.setPreference(GPS_TARGET_DIR, targetDir.getText());
    }
}
