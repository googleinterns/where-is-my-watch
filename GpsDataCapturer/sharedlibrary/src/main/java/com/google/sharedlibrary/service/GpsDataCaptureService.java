package com.google.sharedlibrary.service;

import static com.google.sharedlibrary.locationhelper.FusedLocationProviderHelper.startFusedLocationProviderClient;
import static com.google.sharedlibrary.locationhelper.FusedLocationProviderHelper.stopFusedLocationProviderClient;
import static com.google.sharedlibrary.gpxfile.GpxFile.createGpxFile;
import static com.google.sharedlibrary.gpxfile.GpxFile.getNewFileName;
import static com.google.sharedlibrary.locationhelper.LocationManagerHelper.startLocationManager;
import static com.google.sharedlibrary.locationhelper.LocationManagerHelper.stopLocationManager;
import static com.google.sharedlibrary.storage.GpxFileFolder.createGpsDataFolder;
import static com.google.sharedlibrary.utils.Utils.LocationApiType;
import static com.google.sharedlibrary.utils.Utils.getFormattedCurrentTime;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.sharedlibrary.gpxfile.GpxFile;
import com.google.sharedlibrary.locationhelper.FusedLocationProviderListener;
import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.model.GpsInfoViewModel;

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

    private static File gpxFileFolder;
    private static File gpxFile;

    private static boolean isNewFile;

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
        if (gpxFileFolder == null) {
            gpxFileFolder = createGpsDataFolder(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        fusedLocationProviderListener = null;
//        fusedLocationProviderClient = null;
//        locationManagerListener = null;
//        locationManager = null;
//        stopSelf();
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
     * On location changed, update gps data on gpsDataTextView, draw gps data point on Map and write
     * gps data to file
     *
     * @param location the locationed returned by LocationListener's callback function
     */
    @SuppressLint("MissingPermission")
    public void onLocationChanged(Location location) {
        //set gps data in the view model
        GpsInfoViewModel.setGpsDataMutableLiveData(location);

        //write gps data to file
        GpxFile.writeToFile(gpxFile, this, location, isNewFile);
        isNewFile = false;
    }

    /**
     * On Gps status changed, update the gpsStatus on text view
     *
     * @param event the event returned by GpsStatus Listener's callback function
     */
    public void onGpsStatusChanged(int event) {
        GpsInfoViewModel.setGpsStatusMutableLiveData(event);
    }

    /**
     * Start capturing data from GPS via the chosen location api
     *
     * @param locationApiType the chosen location api
     */
    public void startCapture(LocationApiType locationApiType) {
        gpxFile = createGpxFile(gpxFileFolder, getNewFileName(this));
        isNewFile = true;

        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            startFusedLocationProviderClient(fusedLocationProviderClient,
                    fusedLocationProviderListener);
        } else {
            startLocationManager(locationManager, locationManagerListener);
        }
    }

    /**
     * Stop capturing data from GPS via the chosen location api
     *
     * @param locationApiType the chosen location api
     */
    public void stopCapture(LocationApiType locationApiType) {
        GpxFile.writeFileFooter(gpxFile, this);

        GpxFile.resetGpxFile(gpxFile);

        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            stopFusedLocationProviderClient(fusedLocationProviderClient,
                    fusedLocationProviderListener);
        } else {
            stopLocationManager(locationManager, locationManagerListener);
        }
//        stopSelf();
    }
}
