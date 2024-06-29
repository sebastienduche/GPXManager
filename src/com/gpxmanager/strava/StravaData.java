package com.gpxmanager.strava;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StravaData {
  File zipFile;
  File connectionFile;
  File jsonDataFile;

  public StravaData(File zipFile, File connectionFile, File jsonDataFile) {
    this.zipFile = zipFile;
    this.connectionFile = connectionFile;
    this.jsonDataFile = jsonDataFile;
  }

  public File getZipFile() {
    return zipFile;
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

  public List<String> getFilesToSave() {
    return Stream.of(getJsonDataFile(), getConnectionFile())
        .filter(Objects::nonNull)
        .map(File::getAbsolutePath)
        .toList();
  }
}
