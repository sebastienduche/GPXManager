package com.gpxmanager.component;

import com.gpxmanager.watchdir.WatchDirUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static com.gpxmanager.Utils.getLabel;

public class ConfigureDevicePanel extends JPanel {

  private final JTextField root = new JTextField();
  private final JTextField pathDevice = new JTextField();
  private final JTextField targetDir = new JTextField();
  private final WatchDirUtil watchDirUtil = WatchDirUtil.getInstance();

  public ConfigureDevicePanel() {
    setLayout(new MigLayout("", "[][200:200:]", "[]20px[][][]"));
    add(new JLabel(getLabel("configure.device.title")), "center, span 2, wrap");
    add(new JLabel(getLabel("configure.device.root")), "");
    add(root, "growx, wrap");
    add(new JLabel(getLabel("configure.device.path")), "");
    add(pathDevice, "growx, wrap");
    add(new JLabel(getLabel("configure.device.target")), "");
    add(targetDir, "growx, wrap");
    root.setText(watchDirUtil.getRootDir());
    pathDevice.setText(watchDirUtil.getMountDir());
    targetDir.setText(watchDirUtil.getTargetDir());
  }

  public void save() {
    watchDirUtil.setRootDir(root.getText());
    watchDirUtil.setMountDir(pathDevice.getText());
    watchDirUtil.setTargetDir(targetDir.getText());
  }
}
