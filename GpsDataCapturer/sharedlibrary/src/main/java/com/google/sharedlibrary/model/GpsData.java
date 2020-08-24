package com.google.sharedlibrary.model;

import android.location.Location;
import androidx.annotation.NonNull;
import java.text.DecimalFormat;

/** The data class for GPS data, which will be passed to UI via {@link GpsInfoViewModel} */
public class GpsData {
  private String latitude;
  private String longitude;
  private String speed;
  private DecimalFormat locationDF = new DecimalFormat("0.000000");
  private DecimalFormat speedDF = new DecimalFormat("0.0000");

  /**
   * Constructor to create a GpsData object
   *
   * @param location
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
}
