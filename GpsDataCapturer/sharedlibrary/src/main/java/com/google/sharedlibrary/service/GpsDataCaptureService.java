package com.google.sharedlibrary.service;

import static com.google.sharedlibrary.locationhelper.FusedLocationProviderHelper.startFusedLocationProviderClient;
import static com.google.sharedlibrary.locationhelper.FusedLocationProviderHelper.stopFusedLocationProviderClient;
import static com.google.sharedlibrary.gpxfile.GpxFileHelper.createGpxFile;
import static com.google.sharedlibrary.locationhelper.LocationManagerHelper.startLocationManager;
import static com.google.sharedlibrary.locationhelper.LocationManagerHelper.stopLocationManager;
import static com.google.sharedlibrary.storage.GpxFileFolder.createGpsDataFolder;
import static com.google.sharedlibrary.utils.Utils.LocationApiType;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.sharedlibrary.gpxfile.GpxFileHelper;
import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.locationhelper.FusedLocationProviderListener;
import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.model.GpsInfoViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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

    private File gpxFileFolder;

    protected File gpxFile;

    private GpxFileWriter gpxFileWriter;

    private GpsInfoViewModel gpsInfoViewModel;

    private SimpleDateFormat sdf;

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

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                this.getResources().getConfiguration().locale);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
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
     * @param locationApiType the chosen location api
     */
    public void startCapture(LocationApiType locationApiType) {
        //create a new file
        gpxFile = createGpxFile(gpxFileFolder, sdf.format(System.currentTimeMillis()));

        //instantiate the gpxFileWriter if it's null
        if (gpxFileWriter == null) {
            gpxFileWriter = new GpxFileWriter(sdf, gpxFile, true);
        }

        //write the file header
        gpxFileWriter.writeFileAnnotation(true);

        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            startFusedLocationProviderClient(fusedLocationProviderClient,
                    fusedLocationProviderListener);
        } else {
            startLocationManager(locationManager, locationManagerListener);
        }
    }

    public void setGpsInfoViewModel(GpsInfoViewModel gpsInfoViewModel){
        this.gpsInfoViewModel = gpsInfoViewModel;
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
        gpsInfoViewModel.setGpsDataMutableLiveData(location);

        //write gps data to file
        try {
            gpxFileWriter.writeGpsData(location);
        } catch (Exception e) {
            Log.e(TAG, "GpxFileWriter could not write data.", e);
        }
    }

    /**
     * On Gps status changed, update the gpsStatus on text view
     *
     * @param event the event returned by GpsStatus Listener's callback function
     */
    public void onGpsStatusChanged(int event) {
        //set gps status in the view model
        gpsInfoViewModel.setGpsStatusMutableLiveData(event);
    }

    /**
     * Stop capturing data from GPS via the chosen location api
     *
     * @param locationApiType the chosen location api
     */
    public void stopCapture(LocationApiType locationApiType) {

        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            stopFusedLocationProviderClient(fusedLocationProviderClient,
                    fusedLocationProviderListener);
        } else {
            stopLocationManager(locationManager, locationManagerListener);
        }
        //write the file footer
        gpxFileWriter.writeFileAnnotation(false);

        //reset gpxFileWriter and gpxFile
        gpxFileWriter = null;
        gpxFile = null;
    }
}