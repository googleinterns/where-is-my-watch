package com.google.sharedlibrary.locationhelper;

import android.location.GpsStatus;
import android.location.Location;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.sharedlibrary.service.GpsDataCaptureService;

/**
 * This class extends LocationCallback and implements GpsStatus.Listener and help handle location
 * and Gps status updates via FusedLocationProviderClient API
 */
public class FusedLocationProviderListener extends LocationCallback implements GpsStatus.Listener {
    private GpsDataCaptureService gpsDataCaptureService;

    public FusedLocationProviderListener(GpsDataCaptureService gpsDataCaptureService) {
        this.gpsDataCaptureService = gpsDataCaptureService;
    }
    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }
        for (Location location : locationResult.getLocations()) {
            if (location != null) {
                gpsDataCaptureService.onLocationChanged(location);
            }
        }
    }
    @Override
    public void onLocationAvailability(LocationAvailability var1) {
    }

    @Override
    public void onGpsStatusChanged(int event) {
        gpsDataCaptureService.onGpsStatusChanged(event);
    }
}
