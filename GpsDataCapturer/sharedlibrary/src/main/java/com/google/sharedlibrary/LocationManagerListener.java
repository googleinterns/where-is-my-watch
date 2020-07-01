package com.google.sharedlibrary;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * This class implements LocationListener and GpsStatus.Listener callback functions and handle
 * location updates and Gps status via Location Manager API
 */
public class LocationManagerListener implements LocationListener, GpsStatus.Listener {
    private final String TAG = "GeneralLocationListener";
    private GpsDataCaptureService gpsDataCaptureservice;

    public LocationManagerListener(GpsDataCaptureService service) {
        this.gpsDataCaptureservice = service;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        gpsDataCaptureservice.onGpsStatusChanged(event);
    }

    @Override
    public void onLocationChanged(Location location) {
        gpsDataCaptureservice.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
