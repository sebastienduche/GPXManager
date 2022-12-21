package com.gpxmanager;

import com.gpxmanager.component.JModifyFormattedTextField;
import com.gpxmanager.component.JModifyTextField;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.gpxmanager.Utils.getLabel;

public class GPXPropertiesPanel extends JPanel {

    private final GPX gpx;
    private final File file;
    private final JModifyTextField metadataName = new JModifyTextField();
    private final JModifyTextField metadataDescription = new JModifyTextField();
    private final JModifyTextField metadataAuthor = new JModifyTextField();
    private final JModifyTextField metadataKeywords = new JModifyTextField();
    private final JModifyFormattedTextField metadataTime = new JModifyFormattedTextField(new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss"));

    public GPXPropertiesPanel(File file, GPX gpx) {
        this.file = file;
        this.gpx = gpx;
        setLayout(new MigLayout("", "[grow]", "[]"));
        createPropertiesPanel();
    }

    public GPX getGpx() {
        return gpx;
    }

    public File getFile() {
        return file;
    }

    public void save() throws ParseException {
        Metadata metadata = gpx.getMetadata();
        if (metadata == null) {
            metadata = new Metadata();
            gpx.setMetadata(metadata);
        }
        if (metadataName.isModified()) {
            metadata.setName(metadataName.getText());
        }
        if (metadataDescription.isModified()) {
            metadata.setDescription(metadataDescription.getText());
        }
        if (metadataAuthor.isModified()) {
            metadata.setAuthor(metadataAuthor.getText());
        }
        if (metadataKeywords.isModified()) {
            metadata.setKeywords(metadataKeywords.getText());
        }
        if (metadataTime.isModified() && metadataTime.isValid()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
            metadata.setTime(sdf.parse(metadataKeywords.getText()));
        }
    }

    private void createPropertiesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[][grow]10px[][grow]10px[][grow]", "grow"));
        add(panel, "growx, wrap");
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("properties.title")));
        panel.add(new JLabel(getLabel("properties.trackCount")));
        panel.add(new JLabel(getTrackCount()));
        panel.add(new JLabel(getLabel("properties.routeCount")));
        panel.add(new JLabel(getRouteCount()));
        panel.add(new JLabel(getLabel("properties.waypointCount")));
        panel.add(new JLabel(getWaypointCount()), "wrap");

        Metadata metadata = gpx.getMetadata();
        if (metadata != null) {
            panel.add(new JLabel(getLabel("properties.name")));
            metadataName.setText(metadata.getName());
            panel.add(metadataName, "growx");
            panel.add(new JLabel(getLabel("properties.description")));
            metadataDescription.setText(metadata.getDescription());
            panel.add(metadataDescription, "growx, span 4, wrap");

            panel.add(new JLabel(getLabel("properties.author")));
            metadataAuthor.setText(metadata.getAuthor());
            panel.add(metadataAuthor, "growx");
            panel.add(new JLabel(getLabel("properties.keywords")));
            metadataKeywords.setText(metadata.getKeywords());
            panel.add(metadataKeywords, "growx, span 4, wrap");
            panel.add(new JLabel(getLabel("properties.time")));
            metadataTime.setValue(metadata.getTime());
            panel.add(metadataTime, "growx");
        }
    }

    private String getTrackCount() {
        return gpx.getTracks() == null ? "0" : Integer.toString(gpx.getTracks().size());
    }

    private String getRouteCount() {
        return gpx.getRoutes() == null ? "0" : Integer.toString(gpx.getRoutes().size());
    }

    private String getWaypointCount() {
        return gpx.getWaypoints() == null ? "0" : Integer.toString(gpx.getWaypoints().size());
    }
}