package com.google.sharedlibrary.locationhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.sharedlibrary.service.GpsDataCaptureService;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * This class implements LocationListener and GpsStatus.Listener callback functions and handle
 * location updates and Gps status via Location Manager API
 */
public class LocationManagerListener implements LocationListener, GpsStatus.Listener {
    private final String TAG = "GeneralLocationListener";
    private GpsDataCaptureService gpsDataCaptureservice;

    public LocationManagerListener(GpsDataCaptureService service) {
        Log.d(TAG, "Create LocationManagerListener!");
        this.gpsDataCaptureservice = service;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged");
        int satellitesUsedInFix = 0;
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            //Get Gps Status
            @SuppressLint("MissingPermission")
            GpsStatus status = gpsDataCaptureservice.getLocationManager().getGpsStatus(null);

            Iterable<GpsSatellite> satellites = status.getSatellites();
            PriorityQueue<Float> signalPriorityQueue = new PriorityQueue<Float>((a, b) -> Float.compare(b, a));

            int satellitesVisible = 0;
            float averageSignalStrength = 0;
            float averageTop4SignalStrength = 0;

            //Get the satellites visible, satellites used in fix and the signal to noise ratio for the satellite.
            for(GpsSatellite sat: satellites){
                if(sat.usedInFix()){
                    satellitesUsedInFix++;
                    averageSignalStrength += sat.getSnr();
                    averageTop4SignalStrength = sat.getSnr();

                    if(signalPriorityQueue.size() > 4){
                        signalPriorityQueue.poll();
                    }
                    signalPriorityQueue.add(averageTop4SignalStrength);
                }
                satellitesVisible++;
            }

            //Calculate the average of signal strength and top 4 strongest signal strength
            if(satellitesUsedInFix != 0){
                averageSignalStrength /= satellitesUsedInFix;
                //only take the top 4 strongest signal
                int size = signalPriorityQueue.size();
                while(!signalPriorityQueue.isEmpty()){
                    averageTop4SignalStrength += signalPriorityQueue.poll();
                }
                averageTop4SignalStrength /= size;
            }

            Log.d(TAG, "Satellites visible: " + satellitesVisible);
            Log.d(TAG, "Satellites used in fix: " + satellitesUsedInFix);
            Log.d(TAG, "Average of satellites used in fix signal strength: " + averageSignalStrength);
            Log.d(TAG, "Average of top 4 strongest signal strength: " + averageTop4SignalStrength);

            gpsDataCaptureservice.setAverageOfTop4Signal(averageTop4SignalStrength);
        }
        gpsDataCaptureservice.onGpsStatusChanged(event, satellitesUsedInFix);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d(TAG, "onLocationChanged!");
            gpsDataCaptureservice.onLocationChanged(location);
        }
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
