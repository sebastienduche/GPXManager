package com.gpxmanager.watchdir;

import com.gpxmanager.ProgramPreferences;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.gpxmanager.ProgramPreferences.GPS_MOUNT_DIR;
import static com.gpxmanager.ProgramPreferences.GPS_MOUNT_ROOT;
import static com.gpxmanager.ProgramPreferences.GPS_TARGET_DIR;
import static com.gpxmanager.ProgramPreferences.getPreference;
import static com.gpxmanager.ProgramPreferences.setPreference;
import static com.gpxmanager.Utils.cleanString;

public class WatchDirUtil {

  private final static WatchDirUtil INSTANCE = new WatchDirUtil();
  private WatchDir watchDir;
  private String rootDir;
  private String mountDir;
  private String targetDir;

  public static WatchDirUtil getInstance() {
    return INSTANCE;
  }

  public void initWatchDir(WatchDirListener watchDirListener) throws IOException {
    String path = ProgramPreferences.getPreference(GPS_MOUNT_DIR, "");
    if (rootDir.isEmpty() || path.isEmpty()) {
      return;
    }
    if (watchDir == null) {
      watchDir = new WatchDir(Paths.get(rootDir), false, false);
    }
    watchDir.addWatchDirListener(watchDirListener);
    watchDir.execute();
  }

  public boolean watchDirContains(Path path) {
    if (watchDir == null) {
      return false;
    }
    return watchDir.exist(path);
  }

  public boolean watchDirContainsMountPath() {
    return watchDirContains(Paths.get(mountDir));
  }

  public String getRootDir() {
    if (rootDir == null) {
      rootDir = getPreference(GPS_MOUNT_ROOT, "");
    }
    return rootDir;
  }

  public void setRootDir(String rootDir) {
    this.rootDir = cleanString(rootDir);
    setPreference(GPS_MOUNT_ROOT, this.rootDir);
  }

  public String getMountDir() {
    if (mountDir == null) {
      mountDir = getPreference(GPS_MOUNT_DIR, "");
    }
    return mountDir;
  }

  public void setMountDir(String mountDir) {
    this.mountDir = cleanString(mountDir);
    setPreference(GPS_MOUNT_ROOT, this.mountDir);
  }

  public String getTargetDir() {
    if (targetDir == null) {
      targetDir = getPreference(GPS_TARGET_DIR, "");
    }
    return targetDir;
  }

  public void setTargetDir(String targetDir) {
    this.targetDir = cleanString(targetDir);
    setPreference(GPS_TARGET_DIR, this.targetDir);
  }

  public boolean isInvalid() {
    return getRootDir().isEmpty() || getMountDir().isEmpty() || getTargetDir().isEmpty();
  }

  public boolean isValidMountDir(Path path) {
    return mountDir.equals(path.toString());
  }
}
