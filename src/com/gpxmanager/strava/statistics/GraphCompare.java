package com.gpxmanager.strava.statistics;

public record GraphCompare(int value, String label) {

  @Override
  public String toString() {
    return label;
  }
}
