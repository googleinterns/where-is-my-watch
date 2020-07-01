package com.google.sharedlibrary;

import static com.google.sharedlibrary.FusedLocationProviderHelper.startFusedLocationProviderClient;
import static com.google.sharedlibrary.FusedLocationProviderHelper.stopFusedLocationProviderClient;
import static com.google.sharedlibrary.GpxFile.createGpsDataFolder;
import static com.google.sharedlibrary.GpxFile.createGpxFile;
import static com.google.sharedlibrary.GpxFile.getNewFileName;
import static com.google.sharedlibrary.GpxFile.resetGpxFile;
import static com.google.sharedlibrary.GpxFile.writeFileFooter;
import static com.google.sharedlibrary.GpxFile.writeToFile;
import static com.google.sharedlibrary.LocationManagerHelper.startLocationManager;
import static com.google.sharedlibrary.LocationManagerHelper.stopLocationManager;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;

/**
 * This class provides service for capturing gps data from devices and phones, writing collected gps
 * data to file on local sd card and update gpsDataTextView/gpsStatusTextView on UI, and draw gps
 * data points on the Map view.
 *
 * @author lynnzl
 * @date 2020-06-30
 */
public class GpsDataCaptureService extends Service {
    private static final String TAG = "GpsDataCaptureService";
    private final IBinder binder = new GpsDataCaptureBinder();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private LocationManagerListener locationManagerListener;
    private FusedLocationProviderListener fusedLocationProviderListener;

    private static File gpxDataFolder;
    private static File gpxFile;

    private static TextView gpsDataTextView;
    private static TextView gpsStatusTextView;
    private static String gpsStatus;

    public enum LocationApiVersion {FUSEDLOCATIONPROVIDERCLIENT, LOCATIONMANAGER}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        }
        if (fusedLocationProviderListener == null) {
            fusedLocationProviderListener = new FusedLocationProviderListener(this);
        }
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationManagerListener == null) {
            locationManagerListener = new LocationManagerListener(this);
        }
        if (gpxDataFolder == null) {
            gpxDataFolder = createGpsDataFolder(this);
        }
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
     * On location changed, update gps data on gpsDataTextView, draw gps data point on Map
     * and write gps data to file
     * @param location the locationed returned by LocationListener's callback function
     */
    @SuppressLint("MissingPermission")
    public void onLocationChanged(Location location) {
        //set gps data text on gpsDataTextView
        StringBuilder gpsDataBuilder = new StringBuilder();
        gpsDataBuilder.append("GPS DATA \n")
                .append("Lat: ").append(location.getLatitude())
                .append("\n")
                .append("Lon: ").append(location.getLongitude())
                .append("\n")
                .append("Speed: ").append(location.getSpeed())
                .append("\n");
        gpsDataTextView.setText(gpsDataBuilder.toString());

        //Todo draw gps data point on the map

        //write gps data to file
        writeToFile(gpxFile, this, location);
    }

    /**
     * On Gps status changed, update the gpsStatus on text view
     * @param event the event returned by GpsStatus Listener's callback function
     */
    public void onGpsStatusChanged(int event) {
        //update gpsStatus
        switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.d(TAG, "Gps got the first fix.");
                gpsStatus = "Gps Status: GPS_EVENT_FIRST_FIX";
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                Log.d(TAG, "Gps started, waiting for fix.");
                gpsStatus = "Gps Status: GPS_EVENT_STARTED";
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.d(TAG, "Gps paused.");
                gpsStatus = "Gps Status: GPS_EVENT_STOPPED";
                break;
        }
        //set gpsStatus text on gpsStatusTextView
        gpsStatusTextView.setText(gpsStatus);
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
     * Start capturing data from GPS via the chosen location api
     *
     * @param locationApiVersion the chosen location api
     * @param gpsDataTextView the gps data text view in main activity
     * @param gpsStatusTextView the gps status text view in main activity
     */
    public void startCapture(LocationApiVersion locationApiVersion, TextView gpsDataTextView,
            TextView gpsStatusTextView) {
        GpsDataCaptureService.gpsDataTextView = gpsDataTextView;
        GpsDataCaptureService.gpsStatusTextView = gpsStatusTextView;

        gpxFile = createGpxFile(gpxDataFolder, getNewFileName(this));

        switch (locationApiVersion) {
            case FUSEDLOCATIONPROVIDERCLIENT:
                startFusedLocationProviderClient(fusedLocationProviderClient,
                        fusedLocationProviderListener);
                break;
            case LOCATIONMANAGER:
                startLocationManager(locationManager, locationManagerListener);
        }
    }

    /**
     * Stop capturing data from GPS via the chosen location api
     *
     * @param locationApiVersion the chosen location api
     */
    public void stopCapture(LocationApiVersion locationApiVersion) {
        writeFileFooter(gpxFile, this);

        resetGpxFile(gpxFile);

        switch (locationApiVersion) {
            case FUSEDLOCATIONPROVIDERCLIENT:
                stopFusedLocationProviderClient(fusedLocationProviderClient,
                        fusedLocationProviderListener);
                break;
            case LOCATIONMANAGER:
                stopLocationManager(locationManager, locationManagerListener);
        }

        stopSelf();
    }
}
