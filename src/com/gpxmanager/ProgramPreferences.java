package com.gpxmanager;

import java.util.prefs.Preferences;

public class ProgramPreferences {
  private static final Preferences prefs = Preferences.userNodeForPackage(ProgramPreferences.class);

  public static String FILE = "MyGPXManager.file";
  public static String FILE1 = "MyGPXManager.file1";
  public static String FILE2 = "MyGPXManager.file2";
  public static String FILE3 = "MyGPXManager.file3";
  public static String FILE4 = "MyGPXManager.file4";
  public static String LOCALE = "MyGPXManager.locale";
  public static String LOCATION_X = "MyGPXManager.x";
  public static String LOCATION_Y = "MyGPXManager.y";
  public static String WIDTH = "MyGPXManager.width";
  public static String HEIGHT = "MyGPXManager.height";
  @Deprecated
  public static String STRAVA = "MyGPXManager.strava";
  @Deprecated
  public static String STRAVA_ALL_DATA = "strava.allData";
  public static String STRAVA_ZIP_DATA = "strava.zipData";
  public static String DIR = "MyGPXManager.dir";
  public static String GPS_MOUNT_ROOT = "MyGPXManager.gps.mount.root";
  public static String GPS_MOUNT_DIR = "MyGPXManager.gps.mount.dir";
  public static String GPS_TARGET_DIR = "MyGPXManager.gps.target.dir";

  public static void setPreference(String key, String value) {
    prefs.put(key, value);
  }

  public static void removePreference(String key) {
    prefs.remove(key);
  }

  public static String getPreference(String key, String defaultValue) {
    return prefs.get(key, defaultValue);
  }
}
