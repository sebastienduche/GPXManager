package com.gpxmanager.component;

import com.gpxmanager.Filter;
import com.gpxmanager.MyGPXManager;
import com.gpxmanager.ProgramPreferences;
import com.gpxmanager.Utils;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.gpxmanager.ProgramPreferences.STRAVA_ALL_DATA;
import static com.gpxmanager.Utils.getLabel;

// Configure zip file here
@Deprecated
public class ConfigureStravaStoragePanel extends JPanel {

  private final JTextField filePath = new JTextField();

  public ConfigureStravaStoragePanel() {
    setLayout(new MigLayout("", "[][400:400:]", "[]20px[][][]"));
    add(new JLabel(getLabel("configure.strava.title")), "center, span 2, wrap");
    add(new JLabel(getLabel("configure.strava.file")), "");
    add(filePath, "growx, split 2");
    add(new JButton(new BrowseAction()), "wrap");
    filePath.setText(ProgramPreferences.getPreference(STRAVA_ALL_DATA, ""));
  }

  public void save() {
    File file = new File(filePath.getText());
    if (Utils.checkFileNameWithZIPExtension(file) == null) {
      return;
    }
    ProgramPreferences.setPreference(STRAVA_ALL_DATA, filePath.getText());
  }

  private class BrowseAction extends AbstractAction {
    public BrowseAction() {
      super(getLabel("configure.strava.browse"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
      fileChooser.addChoosableFileFilter(Filter.FILTER_ZIP);
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(MyGPXManager.getInstance())) {
        File selectedFile = fileChooser.getSelectedFile();
        selectedFile = Utils.checkFileNameWithZIPExtension(selectedFile);
        if (selectedFile == null) {
          MyGPXManager.getInstance().setCursor(Cursor.getDefaultCursor());
          return;
        }
        filePath.setText(selectedFile.getAbsolutePath());
      }
    }
  }
}
