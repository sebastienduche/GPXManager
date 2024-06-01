package com.gpxmanager.gpx;

import com.gpxmanager.gpx.beans.Waypoint;

import static com.gpxmanager.Utils.cleanString;

public record CSVLine(String latitude, String longitude, String name, boolean waypoint) {
  private static final String WAYPOINT_CODE = "W";

  public static CSVLine parse(String csvLine) {
    String[] values = csvLine.split(",");
    if (values.length >= 2) {
      return new CSVLine(
          cleanString(values[0]),
          cleanString(values[1]),
          values.length > 2 ? cleanString(values[2]) : null,
          values.length > 3 && cleanString(values[3]).contains(WAYPOINT_CODE));
    }
    return null;
  }

  public static String explain() {
    return """
        CSV line example:\s
        <latitude>,<longitude>,<name>,<options>\s
        2.130114, 5.192440, De Leijen,W\s
        name can be empty, options can be empty\s
        options: W: create a waypoint
        """;
  }

  public Waypoint buildWaypoint() {
    if (!latitude.isEmpty() && !longitude.isEmpty()) {
      Waypoint waypoint = new Waypoint();
      waypoint.setLatitude(Double.parseDouble(latitude));
      waypoint.setLongitude(Double.parseDouble(longitude));
      waypoint.setName(name);
      return waypoint;
    }
    return null;
  }
}
