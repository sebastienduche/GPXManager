package com.gpxmanager.component;

import com.gpxmanager.MyTime;
import com.gpxmanager.actions.SendToDeviceAction;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.gpx.beans.Track;
import com.gpxmanager.watchdir.WatchDirListener;
import com.gpxmanager.watchdir.WatchDirUtil;
import com.mycomponents.JModifyFormattedTextField;
import com.mycomponents.JModifyTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.ParseException;

import static com.gpxmanager.MyGPXManager.setInfoLabel;
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
  private final JModifyFormattedTextField metadataTime = new JModifyFormattedTextField(TIMESTAMP, true);

  public PropertiesPanel(File file, GPX gpx, boolean sendToDeviceVisible) {
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
        totalDistance += getTrackDistance(track.getTrackPoints());
      }
      add(new JBoldLabel(roundValue(totalDistance) + " " + getLabel("km")));
      add(new JLabel(getLabel("properties.totalTime")));
      MyTime totalTime = new MyTime();
      for (Track track : gpx.getTracks()) {
        totalTime = MyTime.add(totalTime, getTrackTime(track.getTrackPoints()));
      }
      if (totalTime != null && totalTime.time() != 0) {
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
    JButton sendToDevice = new JButton(new SendToDeviceAction(file));
    sendToDevice.setVisible(sendToDeviceVisible);
    add(sendToDevice);
    try {
      watchDir(sendToDevice);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void watchDir(JButton sendToDevice) throws IOException {
    WatchDirUtil watchDirUtil = WatchDirUtil.getInstance();
    if (watchDirUtil.isInvalid()) {
      return;
    }
    watchDirUtil.initWatchDir(new WatchDirListener() {
      @Override
      public void eventCreated(Path path) {
        if (watchDirUtil.isValidMountDir(path)) {
          setInfoLabel(MessageFormat.format(getLabel("device.connected"), path));
          sendToDevice.setEnabled(true);
        }
      }

      @Override
      public void eventDeleted(Path path) {
        if (watchDirUtil.isValidMountDir(path)) {
          setInfoLabel(MessageFormat.format(getLabel("device.disconnected"), path));
          sendToDevice.setEnabled(false);
        }
      }

      @Override
      public void eventModified(Path path) {
        setInfoLabel(MessageFormat.format(getLabel("device.updated"), path));
      }
    });
    sendToDevice.setEnabled(watchDirUtil.watchDirContainsMountPath());
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

  public boolean save(Metadata metadata) throws ParseException {
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
      if (metadataTime.getText() == null || metadataTime.getText().isBlank()) {
        metadata.setTime(null);
      } else {
        try {
          metadata.setTime(TIMESTAMP.parse(metadataTime.getText()));
        } catch (ParseException e) {
          JOptionPane.showMessageDialog(this, getLabel("error.timestamp"), getLabel("error.title"), JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
    }
    return true;
  }

  public void load(Metadata metadata) {
    if (metadata == null) {
      return;
    }
    metadataName.setText(metadata.getName());
    metadataDescription.setText(metadata.getDescription());
    metadataAuthor.setText(metadata.getAuthor());
    metadataKeywords.setText(metadata.getKeywords());
    metadataTime.setValue(metadata.getTime());
    metadataName.setModified(false);
    metadataDescription.setModified(false);
    metadataAuthor.setModified(false);
    metadataKeywords.setModified(false);
    metadataTime.setModified(false);
  }

  public boolean isModified() {
    return metadataName.isModified() ||
        metadataDescription.isModified() ||
        metadataAuthor.isModified() ||
        metadataKeywords.isModified() ||
        metadataTime.isModified();
  }
}
