package com.gpxmanager.gpx;

import java.util.Arrays;
import java.util.List;

public enum GPXToolsOption {
  INVERT("-invert"),
  MERGE("-merge"),
  CSV_TO_GPX("-csvgpx"),
  FIND_WAYPOINT("-find"),
  FIX("-fix");

  private final String name;

  GPXToolsOption(String name) {
    this.name = name;
  }

  public static GPXToolsOption findByName(List<String> optionList) {
    return Arrays.stream(values())
        .filter(gpxToolsOption -> optionList.contains(gpxToolsOption.getName()))
        .findFirst()
        .orElse(null);
  }

  public String getName() {
    return name;
  }
}
