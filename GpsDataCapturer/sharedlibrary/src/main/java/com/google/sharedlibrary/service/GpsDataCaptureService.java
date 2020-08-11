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
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.sharedlibrary.R;
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
public class GpsDataCaptureService extends IntentService {
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

    private BroadcastReceiver serviceBroadcastReceiver;
    private IntentFilter intentFilter;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * //     * @param name Used to name the worker thread, important only for debugging.
     */
    public GpsDataCaptureService() {
        super(TAG);
        Log.d(TAG, "Creates the GpsDataCapture IntentService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Return the binder");
        return binder;
    }

    /**
     * This method is invoked on the worker thread with a request to process. Only one Intent is
     * processed at a time, but the processing happens on a worker thread that runs independently
     * from other application logic. So, if this code takes a long time, it will hold up other
     * requests to the same IntentService, but it will not hold up anything else. When all requests
     * have been handled, the IntentService stops itself, so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link Context#startService(Intent)}. This may be null if
     *               the service is being restarted after its process has gone away; see {@link
     *               Service#onStartCommand} for details.
     */

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Log.d(TAG, "On handle intent");

            //Extra the location api type
            boolean type_from_intent = intent.getBooleanExtra("fused_location_type", false);
            Log.d(TAG, "type_from_intent: " + type_from_intent);
            LocationApiType type = LocationApiType.LOCATIONMANAGER;
            if (type_from_intent) {
                type = LocationApiType.FUSEDLOCATIONPROVIDERCLIENT;
            }
            Log.d(TAG, "LocationApiType: " + type);

            if (intent.getAction() != null) {
                if (intent.getAction().equals(
                        "com.google.gpsdatacapturer.STOP_CAPTURE")) {
                    Log.d(TAG, "Intent action: com.google.gpsdatacapturer.STOP_CAPTURE");

                    Handler mHandler = new Handler(getMainLooper());
                    LocationApiType finalType = type;
                    mHandler.post(() -> {
                        stopCapture(finalType);
                        Log.d(TAG, "Stopped capture via intent");
                    });
                }
            }
        }
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

    public void setGpsInfoViewModel(GpsInfoViewModel gpsInfoViewModel) {
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
        if (gpsInfoViewModel != null) {
            gpsInfoViewModel.setGpsDataMutableLiveData(location);
        }

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
    public void onGpsStatusChanged(int event, int satellites) {
        //set gps status in the view model
        if (gpsInfoViewModel != null) {
            gpsInfoViewModel.setGpsStatusMutableLiveData(event);
            gpsInfoViewModel.setSatellitesUsedInFix(satellites);
        }
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
    }

    /**
     * Get the location manager
     * @return the location manager
     */
    public LocationManager getLocationManager(){
        return this.locationManager;
    }
}