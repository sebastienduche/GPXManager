package com.gpxmanager;

import com.gpxmanager.component.JModifyFormattedTextField;
import com.gpxmanager.component.JModifyTextField;
import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class GPXPropertiesPanel extends JPanel {

    private final GPX gpx;
    private final File file;
    private final JModifyTextField metadataName = new JModifyTextField();
    private final JModifyTextField metadataDescription = new JModifyTextField();
    private final JModifyTextField metadataAuthor = new JModifyTextField();
    private final JModifyTextField metadataKeywords = new JModifyTextField();
    private final List<JModifyTextField> trackNames = new LinkedList<>();
    private final List<JModifyTextField> trackDescriptions = new LinkedList<>();
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

        int i = 0;
        for (Track track : gpx.getTracks()) {
            if (trackNames.get(i).isModified()) {
                track.setName(trackNames.get(i).getText());
            }
            if (trackDescriptions.get(i).isModified()) {
                track.setDescription(trackDescriptions.get(i).getText());
            }
            i++;
        }

    }

    private void createPropertiesPanel() {
        JPanel propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new MigLayout("", "[][grow]10px[][grow]10px[][grow]", "grow"));
        add(propertiesPanel, "growx, wrap");
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("properties.title")));
        propertiesPanel.add(new JLabel(getLabel("properties.trackCount")));
        propertiesPanel.add(new JLabel(getTrackCount()));
        propertiesPanel.add(new JLabel(getLabel("properties.routeCount")));
        propertiesPanel.add(new JLabel(getRouteCount()));
        propertiesPanel.add(new JLabel(getLabel("properties.waypointCount")));
        propertiesPanel.add(new JLabel(getWaypointCount()), "wrap");

        Metadata metadata = gpx.getMetadata();
        if (metadata != null) {
            propertiesPanel.add(new JLabel(getLabel("properties.name")));
            metadataName.setText(metadata.getName());
            propertiesPanel.add(metadataName, "growx");
            propertiesPanel.add(new JLabel(getLabel("properties.description")));
            metadataDescription.setText(metadata.getDescription());
            propertiesPanel.add(metadataDescription, "growx, span 4, wrap");

            propertiesPanel.add(new JLabel(getLabel("properties.author")));
            metadataAuthor.setText(metadata.getAuthor());
            propertiesPanel.add(metadataAuthor, "growx");
            propertiesPanel.add(new JLabel(getLabel("properties.keywords")));
            metadataKeywords.setText(metadata.getKeywords());
            propertiesPanel.add(metadataKeywords, "growx, span 4, wrap");
            propertiesPanel.add(new JLabel(getLabel("properties.time")));
            metadataTime.setValue(metadata.getTime());
            propertiesPanel.add(metadataTime, "growx, wrap");

            metadataName.setModified(false);
            metadataDescription.setModified(false);
            metadataAuthor.setModified(false);
            metadataKeywords.setModified(false);
            metadataTime.setModified(false);
        }

        int i = 0;
        for (Track track : gpx.getTracks()) {
            i++;
            JPanel trackPanel = new JPanel();
            trackPanel.setLayout(new MigLayout("", "[][grow]10px[][grow]", "grow"));
            add(trackPanel, "growx, wrap");
            trackPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageFormat.format(getLabel("properties.track.number"), i)));
            trackPanel.add(new JLabel(getLabel("properties.track.distance")));
            trackPanel.add(new JLabel(getTrackDistance(track) + " " + getLabel("km")));
            trackPanel.add(new JLabel(getLabel("properties.track.name")));
            JModifyTextField trackName = new JModifyTextField();
            trackNames.add(trackName);
            trackName.setText(track.getName());
            trackPanel.add(trackName, "growx, wrap");
            trackPanel.add(new JLabel(getLabel("properties.description")));
            JModifyTextField trackDescription = new JModifyTextField();
            trackDescriptions.add(trackDescription);
            trackDescription.setText(track.getDescription());
            trackPanel.add(trackDescription, "span 3, growx, wrap");

            trackName.setModified(false);
            trackDescription.setModified(false);
        }

    }

    private int getTrackDistance(Track track) {
        LinkedList<Degree> points = new LinkedList<>();
        for (Waypoint trackPoint : track.getTrackPoints()) {
            points.add(new Degree(trackPoint.getLongitude(), trackPoint.getLatitude()));
        }
        return (int) EarthCalc.calculateDistance(points) / 1000;
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
