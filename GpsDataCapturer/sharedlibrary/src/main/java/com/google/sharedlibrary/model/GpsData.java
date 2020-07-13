package com.google.sharedlibrary.model;


import android.location.Location;

import androidx.annotation.NonNull;

public class GpsData {
    private String latitude;
    private String longtitude;
    private String speed;

    public GpsData(Location location) {
        latitude = String.valueOf(location.getLatitude());
        longtitude = String.valueOf(location.getLongitude());
        speed = String.valueOf(location.hasSpeed() ? location.getSpeed() : "0.0");
    }

    @NonNull
    public String getGpsDataString() {
        StringBuilder gpsDataBuilder = new StringBuilder();
        gpsDataBuilder.append("GPS DATA \n")
                .append("Lat: ").append(latitude)
                .append("\n")
                .append("Lon: ").append(longtitude)
                .append("\n")
                .append("Speed: ").append(speed)
                .append("\n");
        return gpsDataBuilder.toString();
    }
}
