package com.gpxmanager;

import com.gpxmanager.component.JBoldLabel;
import com.gpxmanager.component.PanelChart;
import com.gpxmanager.component.PropertiesPanel;
import com.gpxmanager.gpx.TrackRoute;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Route;
import com.gpxmanager.gpx.beans.Track;
import com.mycomponents.JModifyTextField;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

public class GPXPropertiesPanel extends JPanel implements ITabListener {

    private final GPX gpx;
    private final File file;
    private final PropertiesPanel propertiesPanel;
    private final List<JModifyTextField> trackNames = new LinkedList<>();
    private final List<JModifyTextField> trackDescriptions = new LinkedList<>();
    private LinkedList<TrackRoute> trackRoutes;

    public GPXPropertiesPanel(File file, GPX gpx) {
        this.file = file;
        this.gpx = gpx;
        setLayout(new MigLayout("", "[grow]", "[]"));
        propertiesPanel = new PropertiesPanel(gpx);
        add(propertiesPanel, "growx, wrap");
        buildTrackRoutes();
        createTracksPanel();
    }

    public GPX getGpx() {
        return gpx;
    }

    public File getFile() {
        return file;
    }

    public boolean isModified() {
        if (propertiesPanel.isModified()) {
            return true;
        }
        for (int i = 0; i < gpx.getTracks().size(); i++) {
            if (trackNames.get(i).isModified()) {
                return true;
            }
            if (trackDescriptions.get(i).isModified()) {
                return true;
            }
        }
        return false;
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
        JPanel tracksPanel = null;
        for (TrackRoute track : trackRoutes) {
            if (tracksPanel == null) {
                tracksPanel = new JPanel();
                tracksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getLabel("properties.track")));
                tracksPanel.setLayout(new MigLayout("", "grow", "grow"));
            }
            JPanel trackPanel = new JPanel();
            trackPanel.setLayout(new MigLayout("", "[][grow]10px[][grow]", "grow"));
            tracksPanel.add(trackPanel, "growx, wrap");
            String title = track.isTrack() ? getLabel("properties.track.number") : getLabel("properties.route.number");
            trackPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), MessageFormat.format(title, track.index() + 1)));
            trackPanel.add(new JLabel(getLabel("properties.track.distance")));
            trackPanel.add(new JBoldLabel(roundValue(getTrackDistance(track.routePoints())) + " " + getLabel("km")));
            trackPanel.add(new JLabel(getLabel("properties.track.name")));
            JModifyTextField trackName = new JModifyTextField();
            trackNames.add(trackName);
            trackName.setText(track.name());
            trackPanel.add(trackName, "growx, wrap");
            trackPanel.add(new JLabel(getLabel("properties.track.time")));
            MyTime trackTime = getTrackTime(track.routePoints());
            trackPanel.add(new JBoldLabel(trackTime == null ? "" : trackTime.toString()));
            trackPanel.add(new JLabel(getLabel("properties.description")));
            JModifyTextField trackDescription = new JModifyTextField();
            trackDescriptions.add(trackDescription);
            trackDescription.setText(track.description());
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

    @Override
    public boolean tabWillClose(TabEvent tabEvent) {
        if (isModified()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, MessageFormat.format(getLabel("question.saveOpenedFile"), getFile()), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                try {
                    save();
                    MyGPXManager.save(getGpx(), getFile());
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return true;
    }

    @Override
    public void tabClosed() {
        MyGPXManager.updateTabbedPane();
    }

    private void buildTrackRoutes() {
        trackRoutes = new LinkedList<>();
        int i = 0;
        if (gpx.getTracks() != null) {
            for (Track track : gpx.getTracks()) {
                trackRoutes.add(new TrackRoute(track, i++));
            }
        }
        if (gpx.getRoutes() != null) {
            for (Route route : gpx.getRoutes()) {
                trackRoutes.add(new TrackRoute(route, i++));
            }
        }
    }

}
