package com.google.sharedlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GpsDataCaptureService extends Service {
    private static final String TAG = "GpsDataCaptureService";
    private final IBinder binder = new GpsDataCaptureBinder();
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private final int INTERVAL = 1000;
    private final int DISTANCE = 0;

    private static File gpxDataFolder;
    private static File gpxFile;
    private String fileName;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Todo: send data to map

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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        super.onLowMemory();
    }

    /**
     * Start capturing data
     */
    public void startCapture() {
        if (gpxDataFolder == null) {
            createGpsDataFolder();
        }

        if (fileName == null || fileName.equals("")) {
            setCurrentFileName();
        }

        if (gpxFile == null) {
            createGpxFile();
        }

        startLocationManager();
    }

    /**
     * Stop capturing data
     */
    public void stopCapture() {
        try {
            FileWriter fileWriter = new FileWriter(gpxFile, true);
            fileWriter.write("</trk><time>" + getFormattedCurrentTime() + "</time>");
            fileWriter.write("</gpx>");
            fileWriter.close();
            Log.i(TAG, "Finished writing to GPX file");
        } catch (IOException e) {
            Log.e(TAG, "Could not write the xml footer.", e);
        }

        stopLocationManager();
        resetFileName();
        resetGpxFile();
        stopSelf();
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
            //Todo: direct user to enable GPS
        }
        Log.i(TAG, "Requesting GPS location updates");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener);
    }

    /**
     * Check GPS status
     */
    private void checkGpsStatus() {
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Stop the locationManager
     */
    private void stopLocationManager() {
        if (locationListener != null) {
            Log.i(TAG, "Removing GPS location updates");
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * Write data to file
     *
     * @param location the location captured from GPS
     */
    private void writeToFile(Location location) {
        GpxFileWriter fileWriter = new GpxFileWriter(gpxFile, true);
        try {
            Log.d(TAG, "Starting file writer");
            fileWriter.write(getApplicationContext(), location);
        } catch (Exception e) {
            Log.e(TAG, "Could not write to file", e);
        }
    }

    /**
     * Create GpsDataFolder if not exist.
     */
    private void createGpsDataFolder() {
        try {
            gpxDataFolder = this.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath());
            assert gpxDataFolder != null;
            if (!gpxDataFolder.exists()) {
                gpxDataFolder.mkdir();
            }
            Log.i(TAG, "Create GpsDataFolder path" + gpxDataFolder.getPath());
        } catch (Exception e) {
            Log.e(TAG, "Could not create new folder.", e);
        }
    }

    /**
     * Create gpxFile if not exist
     */
    private void createGpxFile() {
        try {
            Log.i(TAG, "Create a new gpxFile " + fileName);
            gpxFile = new File(gpxDataFolder.getPath(), fileName + " .xml");
            boolean created = gpxFile.createNewFile();
            if (created) {
                FileWriter fileWriter = new FileWriter(gpxFile, true);
                try {
                    Log.d(TAG, "Writing the xml header");
                    fileWriter.write(GpxFileWriter.xmlHeader(fileName));
                    fileWriter.close();
                } catch (Exception e) {
                    Log.e(TAG, "Could not write xml header", e);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Could not create new file.", ex);
        }
    }

    /**
     * Set the current file name in date format
     */
    private void setCurrentFileName() {
        fileName = getFormattedCurrentTime();
    }

    /**
     * Get formatted time
     */
    private String getFormattedCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * Reset the file name to empty string
     */
    private void resetFileName() {
        fileName = "";
    }

    /**
     * Reset the file to null
     */
    private void resetGpxFile() {
        gpxFile = null;
    }
}
