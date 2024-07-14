package com.gpxmanager.component;

import com.gpxmanager.MyGPXManager;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.gpxmanager.gpx.GPXUtils;
import com.gpxmanager.gpx.beans.GPX;
import com.gpxmanager.gpx.beans.Metadata;
import com.gpxmanager.watchdir.WatchDirListener;
import com.gpxmanager.watchdir.WatchDirUtil;
import com.mycomponents.JModifyTextField;
import com.mycomponents.MyAutoHideLabel;
import com.mytabbedpane.ITabListener;
import com.mytabbedpane.TabEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Objects;

import static com.gpxmanager.Utils.checkFileNameWithExtension;
import static com.gpxmanager.Utils.createFileChooser;
import static com.gpxmanager.Utils.getLabel;

public class InvertPanel extends JPanel implements ITabListener {
  private static final String INVERT_PANEL = "INVERT_PANEL";
  private final PropertiesPanel propertiesPanel = new PropertiesPanel(null, null, false);
  private final MyAutoHideLabel infoLabel = new MyAutoHideLabel();
  private final JModifyTextField fileTextField = new JModifyTextField();
  private final JButton browse = new JButton(new BrowseAction());
  private final JButton invertUpload = new JButton(new InvertUploadAction());
  private File file;
  private GPX gpx;

  public InvertPanel() {
    setLayout(new MigLayout("", "[grow]", "[]20px[]20px[][]"));
    infoLabel.setForeground(Color.red);
    add(fileTextField, "growx, split 2");
    add(browse, "wrap");
    add(propertiesPanel, "growx, wrap");
    JButton invert = new JButton(new InvertAction());
    add(invert, "center, split 2");
    add(invertUpload, "hidemode 3");
    add(infoLabel, "newline, center");
    invertUpload.setVisible(false);
    loadFile();
    initWatchDir();
  }

  private void initWatchDir() {
    try {
      WatchDirUtil watchDirUtil = WatchDirUtil.getInstance();
      watchDirUtil.initWatchDir(new WatchDirListener() {
        @Override
        public void eventCreated(Path path) {
          if (watchDirUtil.isValidMountDir(path)) {
            invertUpload.setVisible(true);
          }
        }

        @Override
        public void eventDeleted(Path path) {
          if (watchDirUtil.isValidMountDir(path)) {
            invertUpload.setVisible(false);
          }
        }

        @Override
        public void eventModified(Path path) {

        }
      });
      if (watchDirUtil.watchDirContainsMountPath()) {
        invertUpload.setVisible(true);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getIdentifier() {
    return INVERT_PANEL;
  }

  @Override
  public boolean tabWillClose(TabEvent tabEvent) {
    return true;
  }

  @Override
  public void tabClosed() {
    MyGPXManager.updateTabbedPane();
  }

  private void loadFile() {
    if (file == null) {
      return;
    }
    gpx = GPXUtils.loadFile(file);
    fileTextField.setText(file.getAbsolutePath());
    if (gpx != null) {
      propertiesPanel.load(gpx.getMetadata());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvertPanel that = (InvertPanel) o;
    return Objects.equals(getIdentifier(), that.getIdentifier());
  }

  @Override
  public int hashCode() {
    return Objects.hash(INVERT_PANEL);
  }

  private class BrowseAction extends AbstractAction {
    public BrowseAction() {
      super(getLabel("invert.file"), MyGPXManagerImage.OPEN);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = createFileChooser();
      fileChooser.setCurrentDirectory(Utils.getOpenSaveDirectory());
      if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
        file = fileChooser.getSelectedFile();
        file = checkFileNameWithExtension(file);
        if (file != null) {
          Utils.setOpenSaveDirectory(file.getParentFile());
          loadFile();
        } else {
          fileTextField.setText("");
        }
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  private class InvertAction extends AbstractAction {
    public InvertAction() {
      super(getLabel("menu.invert"), MyGPXManagerImage.SAVE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (file == null) {
        JOptionPane.showMessageDialog(null, getLabel("invert.no.file"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      JFileChooser fileChooser = createFileChooser();
      fileChooser.setCurrentDirectory(Utils.getOpenSaveDirectory());
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(null)) {
        File file = fileChooser.getSelectedFile();
        file = checkFileNameWithExtension(file);
        if (file != null) {
          Utils.setOpenSaveDirectory(file.getParentFile());
          GPXUtils.invertFile(gpx);
          try {
            Metadata metadata = new Metadata();
            propertiesPanel.save(metadata);
            gpx.setMetadata(metadata);
            GPXUtils.writeFile(gpx, file);

            infoLabel.setText(MessageFormat.format(getLabel("merge.file.saved"), file.getAbsolutePath()), true);
          } catch (ParserConfigurationException | IOException | TransformerException |
                   ParseException ex) {
            throw new RuntimeException(ex);
          }
        }
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  private class InvertUploadAction extends AbstractAction {
    public InvertUploadAction() {
      super(getLabel("invert.invertUpload"), MyGPXManagerImage.UPLOAD);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      file = checkFileNameWithExtension(file);
      if (file == null) {
        JOptionPane.showMessageDialog(null, getLabel("invert.no.file"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      GPXUtils.invertFile(gpx);
      try {
        Metadata metadata = new Metadata();
        propertiesPanel.save(metadata);
        gpx.setMetadata(metadata);
        GPXUtils.uploadToDevice(gpx, file.getName());
        infoLabel.setText(MessageFormat.format(getLabel("invert.file.device.saved"), file.getName()), true);
      } catch (ParserConfigurationException | IOException | TransformerException |
               ParseException ex) {
        throw new RuntimeException(ex);
      }
      setCursor(Cursor.getDefaultCursor());
    }
  }

}
