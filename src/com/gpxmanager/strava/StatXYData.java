package com.gpxmanager.strava;

public class StatXYData {

  private final String serie;
  private final double x;
  private final double y;

  public StatXYData(String serie, double x, double y) {
    this.serie = serie;
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public String getSerie() {
    return serie;
  }
}
