package com.gpxmanager.component;

import com.gpxmanager.MyTime;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Track;
import com.mycomponents.JModifyFormattedTextField;
import com.mycomponents.JModifyTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.text.ParseException;

import static com.gpxmanager.MyTime.getTrackTime;
import static com.gpxmanager.Utils.TIMESTAMP;
import static com.gpxmanager.Utils.getLabel;
import static com.gpxmanager.Utils.getTrackDistance;
import static com.gpxmanager.Utils.roundValue;

public class PropertiesPanel extends JPanel {

    private final JModifyTextField metadataName = new JModifyTextField();
    private final JModifyTextField metadataDescription = new JModifyTextField();
    private final JModifyTextField metadataAuthor = new JModifyTextField();
    private final JModifyTextField metadataKeywords = new JModifyTextField();
    private final JModifyFormattedTextField metadataTime = new JModifyFormattedTextField(TIMESTAMP);

    public PropertiesPanel(GPX gpx) {
        setLayout(new MigLayout("", "[][grow]10px[][grow]10px[][grow]", "grow"));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("properties.title")));
        if (gpx != null) {
            add(new JLabel(getLabel("properties.trackCount")));
            add(new JBoldLabel(getTrackCount(gpx)));
            add(new JLabel(getLabel("properties.routeCount")));
            add(new JBoldLabel(getRouteCount(gpx)));
            add(new JLabel(getLabel("properties.waypointCount")));
            add(new JBoldLabel(getWaypointCount(gpx)), "wrap");
            add(new JLabel(getLabel("properties.totalDistance")));
            double totalDistance = 0;
            for (Track track : gpx.getTracks()) {
                totalDistance += getTrackDistance(track);
            }
            add(new JBoldLabel(roundValue(totalDistance) + " " + getLabel("km")));
            add(new JLabel(getLabel("properties.totalTime")));
            MyTime totalTime = new MyTime();
            for (Track track : gpx.getTracks()) {
                totalTime.add(getTrackTime(track));
            }
            if (totalTime.getTime() != 0) {
                add(new JBoldLabel(totalTime.toString()), "wrap");
            } else {
                add(new JLabel(), "wrap");
            }
        }

        Metadata metadata = gpx != null ? gpx.getMetadata() : new Metadata();
        if (metadata != null) {
            add(new JLabel(getLabel("properties.name")));
            metadataName.setText(metadata.getName());
            add(metadataName, "growx");
            add(new JLabel(getLabel("properties.description")));
            metadataDescription.setText(metadata.getDescription());
            add(metadataDescription, "growx, span 4, wrap");

            add(new JLabel(getLabel("properties.author")));
            metadataAuthor.setText(metadata.getAuthor());
            add(metadataAuthor, "growx");
            add(new JLabel(getLabel("properties.keywords")));
            metadataKeywords.setText(metadata.getKeywords());
            add(metadataKeywords, "growx, span 4, wrap");
            add(new JLabel(getLabel("properties.time")));
            metadataTime.setValue(metadata.getTime());
            add(metadataTime, "growx, wrap");

            metadataName.setModified(false);
            metadataDescription.setModified(false);
            metadataAuthor.setModified(false);
            metadataKeywords.setModified(false);
            metadataTime.setModified(false);
        }
    }

    private String getTrackCount(GPX gpx) {
        return gpx.getTracks() == null ? "0" : Integer.toString(gpx.getTracks().size());
    }

    private String getRouteCount(GPX gpx) {
        return gpx.getRoutes() == null ? "0" : Integer.toString(gpx.getRoutes().size());
    }

    private String getWaypointCount(GPX gpx) {
        return gpx.getWaypoints() == null ? "0" : Integer.toString(gpx.getWaypoints().size());
    }

    public void save(Metadata metadata) throws ParseException {
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
            metadata.setTime(TIMESTAMP.parse(metadataKeywords.getText()));
        }
    }
}
