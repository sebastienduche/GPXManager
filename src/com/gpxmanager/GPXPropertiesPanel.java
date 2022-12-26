package com.gpxmanager;

import com.gpxmanager.component.PropertiesPanel;
import com.gpxmanager.geocalc.Degree;
import com.gpxmanager.geocalc.EarthCalc;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.gpx.beans.Waypoint;
import com.mycomponents.JModifyTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import static com.gpxmanager.Utils.getLabel;

public class GPXPropertiesPanel extends JPanel {

    private final GPX gpx;
    private final File file;
    private final PropertiesPanel propertiesPanel;
    private final List<JModifyTextField> trackNames = new LinkedList<>();
    private final List<JModifyTextField> trackDescriptions = new LinkedList<>();

    public GPXPropertiesPanel(File file, GPX gpx) {
        this.file = file;
        this.gpx = gpx;
        setLayout(new MigLayout("", "[grow]", "[]"));
        propertiesPanel = new PropertiesPanel(gpx);
        add(propertiesPanel, "growx, wrap");
        createTracksPanel();
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
        propertiesPanel.save(metadata);

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

    private void createTracksPanel() {
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

}
