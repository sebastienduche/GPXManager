package com.gpxmanager.component;

import com.gpxmanager.Filter;
import com.gpxmanager.MyGPXManagerImage;
import com.gpxmanager.Utils;
import com.mycomponents.JModifyTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.gpxmanager.Utils.getLabel;

public class FilePanel extends JPanel {
  private final JModifyTextField fileTextField = new JModifyTextField();
  private final Type type;
  private File file;

  public FilePanel(Type type) {
    this(type, getLabel("select.file"), type == Type.SAVE_ZIP ? getLabel("select.file.zip") : null);
  }


  public FilePanel(Type type, String fileLabel, String descriptionLabel) {
    this.type = type;
    setLayout(new MigLayout("", "[500:500:500]", "[]"));
    if (descriptionLabel != null) {
      add(new JLabel(descriptionLabel), "wrap");
    }
    if (fileLabel != null) {
      add(new JLabel(fileLabel), "wrap");
    }
    add(fileTextField, "growx, split 2");
    JButton browse = new JButton(new BrowseAction());
    add(browse, "wrap");
  }

  private void updateFileTextField() {
    if (file != null) {
      fileTextField.setText(file.getAbsolutePath());
    } else {
      fileTextField.setText("");
    }
  }

  public File getFile() {
    return file;
  }

  public enum Type {OPEN, SAVE, SAVE_ZIP}

  private class BrowseAction extends AbstractAction {
    public BrowseAction() {
      super(getLabel("invert.file"), MyGPXManagerImage.OPEN);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      boiteFichier.setCurrentDirectory(Utils.getOpenSaveDirectory());
      if (type == Type.SAVE_ZIP) {
        boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
        boiteFichier.addChoosableFileFilter(Filter.FILTER_ZIP);
      }
      if (type == Type.OPEN) {
        if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
          file = boiteFichier.getSelectedFile();
          updateFileTextField();
          setCursor(Cursor.getDefaultCursor());
        }
      } else {
        if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(null)) {
          file = boiteFichier.getSelectedFile();
          updateFileTextField();
          setCursor(Cursor.getDefaultCursor());
        }
      }
    }
  }
}
