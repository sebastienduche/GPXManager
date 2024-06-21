package com.gpxmanager.actions;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.gpxmanager.gpx.GPXUtils;
import com.gpxmanager.gpx.beans.GPX;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import static com.gpxmanager.Utils.createFileChooser;
import static com.gpxmanager.Utils.getLabel;

public class SendToDeviceAction extends AbstractAction {
  private final File gpxFile;

  public SendToDeviceAction() {
    super(getLabel("menu.sendToDevice"), MyGPXManagerImage.UPLOAD);
    gpxFile = null;
  }

  public SendToDeviceAction(File gpxFile) {
    super(getLabel("menu.sendToDevice"), MyGPXManagerImage.UPLOAD);
    this.gpxFile = gpxFile;
  }

  private static void sendToDevice(List<File> openedFile) {
    openedFile.forEach(file -> {
      GPX gpx = GPXUtils.loadFile(file);
      if (gpx != null) {
        try {
          GPXUtils.uploadToDevice(gpx, file.getName());
          MyGPXManager.setInfoLabel(MessageFormat.format(getLabel("file.device.saved"), file.getName()));
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (gpxFile != null) {
      sendToDevice(List.of(gpxFile));
    }
    JFileChooser fileChooser = createFileChooser();
    fileChooser.setMultiSelectionEnabled(true);
    if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(MyGPXManager.getInstance())) {
      List<File> selectedFiles = List.of(fileChooser.getSelectedFiles());
      boolean hasError = selectedFiles.stream().map(Utils::checkFileNameWithExtension)
          .anyMatch(Objects::isNull);
      if (hasError) {
        MyGPXManager.getInstance().setCursor(Cursor.getDefaultCursor());
        return;
      }
      sendToDevice(selectedFiles);
    }
  }
}
