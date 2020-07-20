package com.google.sharedlibrary.locationhelper;

import android.annotation.SuppressLint;
import android.location.LocationManager;
import android.util.Log;

/**
 * Helper class that help the GpsDataCaptureService to start/stop the LocationManager.
 */
public class LocationManagerHelper {
    private static final String TAG = "LocationManagerHelper";
    private static final int INTERVAL = 1000;
    private static final float DISTANCE = 0.0f;

    /**
     * Start Location Manager
     */
    @SuppressLint("MissingPermission")
    public static void startLocationManager(LocationManager locationManager,
            LocationManagerListener locationManagerListener) {
        Log.d(TAG, "Add GpsStatusListener");
        locationManager.addGpsStatusListener(locationManagerListener);

        Log.d(TAG, "RequestLocationUpdates");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE,
                locationManagerListener);
    }

    /**
     * Stop Location Manager
     */
    public static void stopLocationManager(LocationManager locationManager,
            LocationManagerListener locationListener) {
        if (locationListener != null) {
            Log.d(TAG, "LocationManager is removing location updates.");
            locationManager.removeUpdates(locationListener);
            locationManager.removeGpsStatusListener(locationListener);
        }
    }
}
