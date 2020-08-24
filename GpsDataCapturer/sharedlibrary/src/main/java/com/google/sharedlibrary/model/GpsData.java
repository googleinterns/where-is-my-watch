package com.google.sharedlibrary.model;

import android.location.Location;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

/** The data class for GPS data, which will be passed to UI via {@link GpsInfoViewModel} */
public class GpsData {
  private final String latitude;
  private final String longitude;
  private final String speed;
  private final DecimalFormat locationDF = new DecimalFormat("0.000000");
  private final DecimalFormat speedDF = new DecimalFormat("0.0000");

  /**
   * Constructor to create a GpsData object
   *
   * @param location the location passed from gpsInfoViewModel
   */
  public GpsData(Location location) {
    latitude = String.valueOf(locationDF.format(location.getLatitude()));
    longitude = String.valueOf(locationDF.format(location.getLongitude()));
    speed = String.valueOf(location.hasSpeed() ? speedDF.format(location.getSpeed()) : "0.0000");
  }

  /**
   * Get the latitude of the GpsData
   *
   * @return latitude
   */
  @NonNull
  public String getLatitude() {
    return this.latitude;
  }

  /**
   * Get the longitude of the GpsData
   *
   * @return longitude
   */
  @NonNull
  public String getLongitude() {
    return this.longitude;
  }

  /**
   * Get the speed of the GpsData
   *
   * @return speed
   */
  @NonNull
  public String getSpeed() {
    return this.speed;
  }

  /**
   * Get the string of GpsData
   *
   * @return a string of GpsData
   */
  @NonNull
  @Override
  public String toString() {
    StringBuilder gpsDataBuilder = new StringBuilder();
    gpsDataBuilder
        .append("GPS DATA")
        .append(" Lat: ")
        .append(latitude)
        .append(" Lon: ")
        .append(longitude)
        .append(" Speed: ")
        .append(speed);
    return gpsDataBuilder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    GpsData gpsData = (GpsData) o;

    return this.latitude.equals(gpsData.latitude)
        && this.longitude.equals(gpsData.longitude)
        && this.speed.equals(gpsData.speed);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
