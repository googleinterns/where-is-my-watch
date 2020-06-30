package com.google.sharedlibrary;

import static com.google.sharedlibrary.GpxFile.createGpsDataFolder;
import static com.google.sharedlibrary.GpxFile.createGpxFile;
import static com.google.sharedlibrary.GpxFile.getNewFileName;
import static com.google.sharedlibrary.GpxFile.resetGpxFile;
import static com.google.sharedlibrary.GpxFile.writeFileFooter;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.File;

public class GpsDataCaptureService extends Service {
    private static final String TAG = "GpsDataCaptureService";
    private final IBinder binder = new GpsDataCaptureBinder();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private final int INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 1000;

    private static File gpxDataFolder;
    private static File gpxFile;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    //Todo show text view of lat/lon/speed
//                    gpsDataTextView.setText(String.format(Locale.US, "%s -- %s", location
//                    .getLatitude(), location.getLongitude()));
                    //Todo draw data point on the map

                    //write data point to gpx file
                    writeToFile(location);
                }
            }
        }
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability){

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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        fusedLocationProviderClient = null;
        super.onDestroy();
    }

    /**
     * Class used for the client Binder.  Because we know this service always runs in the same
     * process as its clients, we don't need to deal with IPC.
     */
    public class GpsDataCaptureBinder extends Binder {
        public GpsDataCaptureService getService() {
            return GpsDataCaptureService.this;
        }
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "The device is low on memory!");
        super.onLowMemory();
    }

    /**
     * Start capturing data
     */
    public void startCapture() {
        if (gpxDataFolder == null) {
            gpxDataFolder = createGpsDataFolder(this);
        }

        gpxFile = createGpxFile(gpxDataFolder, getNewFileName(this));

        startFusedLocationProviderClient();
    }

    /**
     * Stop capturing data
     */
    public void stopCapture() {
        writeFileFooter(gpxFile, this);

        resetGpxFile(gpxFile);

        stopFusedLocationProviderClient();

        stopSelf();
    }

    /**
     * Start fusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    private void startFusedLocationProviderClient() {
        createLocationRequest();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper())
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "FusedLocationProviderClient could not be started.", e);
            }
        })
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "FusedLocationProviderClient request location update completed.");
            }
        });
    }

    /**
     * Create location request
     */
    private void createLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    /**
     * Stop fusedLocationProviderClient
     */
    private void stopFusedLocationProviderClient() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "FusedLocationProviderClient could not be removed.", e);
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "FusedLocationProviderClient removed location updates completed.");
                }
            });
        }
    }

    /**
     * Write the data to file
     *
     * @param location the location captured from GPS
     */
    private void writeToFile(Location location) {
        GpxFileWriter gpxFileWriter = new GpxFileWriter(gpxFile, true);
        try {
            Log.d(TAG, "Starting gpx file writer");
            gpxFileWriter.write(getApplicationContext(), location);
        } catch (Exception e) {
            Log.e(TAG, "Could not write to file", e);
        }
    }
}
