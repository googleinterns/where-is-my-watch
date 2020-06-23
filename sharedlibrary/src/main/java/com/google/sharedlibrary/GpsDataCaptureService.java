package com.google.sharedlibrary;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class GpsDataCaptureService extends Service {
    private static final String TAG = "GpsDataCaptureService";
    private final IBinder binder = new GpsDataCaptureBinder();
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private final int INTERVAL = 1000;
    private final int DISTANCE = 0;
    private Handler handler = new Handler();
//    private Runnable ;
    private LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
        long currentTimeStamp = System.currentTimeMillis();
        //send data to map

        //write data to file
        writeToFile(location);
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
};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class GpsDataCaptureBinder extends Binder {
        public GpsDataCaptureService getService() {
            return GpsDataCaptureService.this;
        }
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "Watch is low on memory!");
    }

    /**
     * Start capturing data
     */
    public void startCapture() {
        startLocationManager();
    }

    /**
     * Stop capturing data
     */
    public void stopCapture(){
        stopLocationManager();
    }
    /**
     * Start the locationManager
     */
    @SuppressLint("MissingPermission")
    private void startLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //check if GPS Provider enabled
        checkGpsStatus();

        if (!isGpsEnabled) {
           Log.e(TAG, "Please enable GPS Provider!");
           //
        }
        Log.i(TAG, "Requesting GPS location updates");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener);
    }

    /**
     * Check GPS status
     */
    private void checkGpsStatus(){
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Stop the locationManager
     */
    private void stopLocationManager(){
        if(locationListener != null){
            Log.i(TAG, "Removing GPS location updates");
            locationManager.removeUpdates(locationListener);
        }
    }


    /**
     * Write data to file
     * @param loc the location captured from GPS
     * @return
     */
    private void writeToFile(Location loc){
        try{
            Log.d(TAG, "Starting file writer");
            GpxFileWriter.write(loc);
        }catch (Exception e){
            Log.e(TAG, "Could not write to file", e);
        }
    }
}
