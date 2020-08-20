package com.google.sharedlibrary.model;

import android.location.Location;
import androidx.annotation.NonNull;

import java.text.DecimalFormat;

/**
 * The data class for GPS data, which will be passed to UI via {@link GpsInfoViewModel}
 */
public class GpsData {
    private String latitude;
    private String longitude;
    private String speed;
    private DecimalFormat locationDF= new DecimalFormat("0.000000");
    private DecimalFormat speedDF = new DecimalFormat("0.0000");

    public GpsData(Location location) {
        latitude = String.valueOf(locationDF.format(location.getLatitude()));
        longitude = String.valueOf(locationDF.format(location.getLongitude()));
        speed = String.valueOf(location.hasSpeed() ? speedDF.format(location.getSpeed()) : "0.0000");
    }

    /**
     * Get the string of GpsData
     * @return a string of GpsData
     */
    @NonNull
    public String getGpsDataString() {
        StringBuilder gpsDataBuilder = new StringBuilder();
        gpsDataBuilder.append("GPS DATA \n")
                .append("Lat: ").append(latitude)
                .append("\n")
                .append("Lon: ").append(longitude)
                .append("\n")
                .append("Speed: ").append(speed)
                .append("\n");
        return gpsDataBuilder.toString();
    }
}
