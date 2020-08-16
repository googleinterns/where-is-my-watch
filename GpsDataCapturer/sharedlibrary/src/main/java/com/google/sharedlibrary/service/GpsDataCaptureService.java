package com.google.sharedlibrary.service;

import static com.google.sharedlibrary.gpxfile.GpxFileHelper.createFileName;
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
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.locationhelper.FusedLocationProviderListener;
import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.model.GpsInfoViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.PriorityQueue;
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
    private LocationApiType locationApiType = LocationApiType.LOCATIONMANAGER;

    private File gpxFileFolder;
    protected File gpxFile;
    private GpxFileWriter gpxFileWriter;

    private GpsInfoViewModel gpsInfoViewModel;

    private SimpleDateFormat sdf;

    private float averageOfTop4Signal;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Return the binder");
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
     */
    public void startCapture() {
        //create a new file
        gpxFile = createGpxFile(gpxFileFolder, createFileName());

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

    /**
     * On location changed, update gps data on gpsDataTextView, draw gps data point on Map and write
     * gps data to file
     *
     * @param location the locationed returned by LocationListener's callback function
     */
    @SuppressLint("MissingPermission")
    public void onLocationChanged(Location location) {
        //set gps data in the view model
        if (gpsInfoViewModel != null) {
            gpsInfoViewModel.setGpsDataMutableLiveData(location);
        }

        //write gps data to file
        try {
            if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
                averageOfTop4Signal = 0.0f;
            }
            gpxFileWriter.writeGpsData(location, averageOfTop4Signal);
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
        int satellitesUsedInFix = 0;
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            //Get Gps Status
            @SuppressLint("MissingPermission")
            GpsStatus status = locationManager.getGpsStatus(null);

            assert status != null;
            Iterable<GpsSatellite> satellites = status.getSatellites();
            PriorityQueue<Float> signalPriorityQueue = new PriorityQueue<Float>(
                    (a, b) -> Float.compare(b, a));

            int satellitesVisible = 0;
            float averageSignalStrength = 0;

            //Get the satellites visible, satellites used in fix and the signal to noise ratio
            // for the satellite.
            for (GpsSatellite sat : satellites) {
                if (sat.usedInFix()) {
                    satellitesUsedInFix++;
                    float signalStrength = sat.getSnr();
                    averageSignalStrength += signalStrength;

                    if (signalPriorityQueue.size() > 4) {
                        signalPriorityQueue.poll();
                    }
                    signalPriorityQueue.add(signalStrength);
                }
                satellitesVisible++;
            }

            //Calculate the average of signal strength and top 4 strongest signal strength
            if (satellitesUsedInFix != 0) {
                averageSignalStrength /= satellitesUsedInFix;

                int size = signalPriorityQueue.size();
                while (!signalPriorityQueue.isEmpty()) {
                    averageOfTop4Signal += signalPriorityQueue.poll();
                }
                averageOfTop4Signal /= size;
            }

            Log.d(TAG, "Satellites visible: " + satellitesVisible);
            Log.d(TAG, "Satellites used in fix: " + satellitesUsedInFix);
            Log.d(TAG,
                    "Average of satellites used in fix signal strength: " + averageSignalStrength);
            Log.d(TAG, "Average of top 4 strongest signal strength: " + averageOfTop4Signal);
        }

        //set gps status and satellites in the view model
        if (gpsInfoViewModel != null) {
            gpsInfoViewModel.setGpsStatusMutableLiveData(event);
            gpsInfoViewModel.setSatellitesUsedInFix(satellitesUsedInFix);
        }
    }

    /**
     * Stop capturing data from GPS via the chosen location api
     */
    public void stopCapture() {
        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            stopFusedLocationProviderClient(fusedLocationProviderClient,
                    fusedLocationProviderListener);
            Log.d(TAG, "Stopped fused location provider successfully!");
        } else {
            stopLocationManager(locationManager, locationManagerListener);
        }

        //write the file footer
        if (gpxFileWriter != null) {
            gpxFileWriter.writeFileAnnotation(false);
        }

        //reset gpxFileWriter and gpxFile
        gpxFileWriter = null;
        gpxFile = null;
        locationApiType = LocationApiType.LOCATIONMANAGER;
    }

    /**
     * Set the locationApiType
     */
    public void setLocationApiType(LocationApiType locationApiType) {
        this.locationApiType = locationApiType;
    }

    /**
     * Set the gpsInfoViewModel
     */
    public void setGpsInfoViewModel(GpsInfoViewModel gpsInfoViewModel) {
        this.gpsInfoViewModel = gpsInfoViewModel;
    }
}