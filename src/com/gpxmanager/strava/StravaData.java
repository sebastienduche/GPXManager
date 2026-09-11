package com.gpxmanager.strava;

import org.jstrava.entities.Activity;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StravaData {
  private final File zipFile;
  private final File connectionFile;
  private final File jsonDataFile;
  //  private File saveFile;
  private List<Activity> activities;

  public StravaData(File zipFile, File connectionFile, File jsonDataFile) {
    this.zipFile = zipFile;
//    this.saveFile = zipFile;
    this.connectionFile = connectionFile;
    this.jsonDataFile = jsonDataFile;
  }

  public StravaData(File zipFile, File saveFile, File connectionFile, File jsonDataFile) {
    this.zipFile = zipFile;
//    this.saveFile = saveFile;
    this.connectionFile = connectionFile;
    this.jsonDataFile = jsonDataFile;
  }

//  public File getSaveFile() {
//    return saveFile;
//  }
//
//  public void setSaveFile(File saveFile) {
//    this.saveFile = saveFile;
//  }

  public File getConnectionFile() {
    return connectionFile;
  }

  public File getJsonDataFile() {
    return jsonDataFile;
  }

  public boolean hasJsonDataFile() {
    return jsonDataFile != null;
  }

  public boolean hasActivities() {
    return activities != null && !activities.isEmpty();
  }

  public List<Activity> getActivities() {
    return activities;
  }

  public void setActivities(List<Activity> activities) {
    this.activities = activities;
  }

  public List<String> getFilesToSave() {
    return Stream.of(getJsonDataFile(), getConnectionFile())
        .filter(Objects::nonNull)
        .map(File::getAbsolutePath)
        .toList();
  }

}
