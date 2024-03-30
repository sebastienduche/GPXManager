package com.gpxmanager.strava;

public class StatData {

  private final double count;
  private final String name;
  private final String serie;

  public StatData(double count, String name, String serie) {
    this.count = count;
    this.name = name;
    this.serie = serie;
  }

  public double getCount() {
    return count;
  }

  public String getName() {
    return name;
  }

  public String getSerie() {
    return serie;
  }
}
