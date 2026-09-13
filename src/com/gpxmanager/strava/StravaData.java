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
    this.connectionFile = connectionFile;
    this.jsonDataFile = jsonDataFile;
  }

  public StravaData(File jsonDataFile) {
    this.zipFile = null;
    this.connectionFile = null;
    this.jsonDataFile = jsonDataFile;
  }

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
