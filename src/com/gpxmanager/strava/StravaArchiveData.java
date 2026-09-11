package com.gpxmanager.strava;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StravaArchiveData extends StravaData {
  File zipFileToSave;

  public StravaArchiveData(File zipFile, File zipFileToSave, File jsonDataFile) {
    super(zipFile, null, jsonDataFile);
    this.zipFileToSave = zipFileToSave;
  }

  public File getZipFileToSave() {
    return zipFileToSave;
  }

  public List<String> getFilesToSave() {
    return Stream.of(getJsonDataFile())
        .filter(Objects::nonNull)
        .map(File::getAbsolutePath)
        .toList();
  }

}
