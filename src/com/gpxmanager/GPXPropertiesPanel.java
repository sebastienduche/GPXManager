package com.gpxmanager;

import com.gpxmanager.component.PanelChart;
import com.gpxmanager.component.PropertiesPanel;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Track;
import com.mycomponents.JModifyTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import static com.gpxmanager.MyTime.getTrackTime;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getTrackDistance;
import static com.gpxmanager.Utils.roundValue;

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
        JPanel tracksPanel = null;
        for (Track track : gpx.getTracks()) {
            i++;
            if (tracksPanel == null) {
                tracksPanel = new JPanel();
                tracksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("properties.track")));
                tracksPanel.setLayout(new MigLayout("", "grow", "grow"));
            }
            JPanel trackPanel = new JPanel();
            trackPanel.setLayout(new MigLayout("", "[][grow]10px[][grow]", "grow"));
            tracksPanel.add(trackPanel, "growx, wrap");
            trackPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageFormat.format(getLabel("properties.track.number"), i)));
            trackPanel.add(new JLabel(getLabel("properties.track.distance")));
            trackPanel.add(new JLabel(roundValue(getTrackDistance(track)) + " " + getLabel("km")));
            trackPanel.add(new JLabel(getLabel("properties.track.name")));
            JModifyTextField trackName = new JModifyTextField();
            trackNames.add(trackName);
            trackName.setText(track.getName());
            trackPanel.add(trackName, "growx, wrap");
            trackPanel.add(new JLabel(getLabel("properties.track.time")));
            MyTime trackTime = getTrackTime(track);
            trackPanel.add(new JLabel(trackTime == null ? "" : trackTime.toString()));
            trackPanel.add(new JLabel(getLabel("properties.description")));
            JModifyTextField trackDescription = new JModifyTextField();
            trackDescriptions.add(trackDescription);
            trackDescription.setText(track.getDescription());
            trackPanel.add(trackDescription, "span 2, growx, wrap");
            trackPanel.add(new PanelChart(track), "span 4");

            trackName.setModified(false);
            trackDescription.setModified(false);
        }
        if (tracksPanel != null) {
            JScrollPane scrollPane = new JScrollPane(tracksPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, "growx");
        }

    }

}
