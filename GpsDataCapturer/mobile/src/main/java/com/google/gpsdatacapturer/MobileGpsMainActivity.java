package com.google.gpsdatacapturer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.sharedlibrary.GpsDataCaptureService;

public class MobileGpsMainActivity extends AppCompatActivity {
    private static final String TAG = "MobileGpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private LocationManager locationManager;
    private static boolean isBound = false;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int READ_WRITE_REQUEST_CODE = 2;
    private static boolean isStart = true;
    private boolean isGpsEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_gps_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestLocationPermissionsIfNotGranted();
        requestReadWritePermissionsIfNotGranted();

        checkGpsStatus();
        setGpsEnabled();

        startAndBindGpsDataCaptureService();
        final Button startAndStopButton = (Button) findViewById(R.id.mobile_start_stop_button);
        startAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStart) {
                    onStartButtonClick(v);
                    startAndStopButton.setText(R.string.stop_capture);
                    isStart = false;
                    startAndStopButton.setBackground(
                            getResources().getDrawable(R.drawable.mobile_button_red));
                } else {
                    onStopButtonClick(v);
                    startAndStopButton.setText(R.string.start_capture);
                    isStart = true;
                    startAndStopButton.setBackground(
                            getResources().getDrawable(R.drawable.mobile_button_green));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startAndBindGpsDataCaptureService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAndUnbindGpsDataCaptureService();
        isBound = false;
    }

    private void requestLocationPermissionsIfNotGranted() {
        boolean granted = hasUserGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasUserGrantedPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!granted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }

    }

    private void requestReadWritePermissionsIfNotGranted() {
        boolean granted = hasUserGrantedPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasUserGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (!granted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Check if the user grant permissions required to run the app
     *
     * @param permissionName
     * @return return true if permission granted and false if not
     */
    private boolean hasUserGrantedPermission(String permissionName) {
        boolean granted = ActivityCompat.checkSelfPermission(this, permissionName)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Permission " + permissionName + " : " + granted);
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location Permissions are not granted.");
                }
            }
            break;
            case READ_WRITE_REQUEST_CODE: {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Read and Write Permission are not granted.");
                }
            }
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    /**
     * Set GPS if it's not enabled
     */
    private void setGpsEnabled() {
        if (!isGpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
    }

    /**
     * Check GPS status
     */
    private void checkGpsStatus() {
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * On click of Start button, start capturing gps data
     */
    public void onStartButtonClick(View view) {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound, could not start capture.");
            return;
        }
        Log.d(TAG, "Start capture data.");
        gpsDataCaptureService.startCapture();

    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void onStopButtonClick(View view) {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound");
        }
        Log.d(TAG, "Stop capture data.");
        gpsDataCaptureService.stopCapture();
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GpsDataCaptureService.");
            GpsDataCaptureService.GpsDataCaptureBinder
                    binder = (GpsDataCaptureService.GpsDataCaptureBinder) service;
            gpsDataCaptureService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from GpsDataCaptureService");
            isBound = false;
        }
    };

    /**
     * Bind the activity to GpsDataCaptureService
     */
    private void startAndBindGpsDataCaptureService() {
        serviceIntent = new Intent(this, GpsDataCaptureService.class);
        //start GpsDataCaptureService
        try {
            startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not start gpsDataCaptureService", e);
        }
        //Bind to GpsDataCaptureService
        try {
            bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Could not bind gpsDataCaptureService", e);
        }
    }

    /**
     * Unbind the activity from GpsDataCaptureService
     */
    private void stopAndUnbindGpsDataCaptureService() {
        //Unbind from GpsDataCaptureService
        try {
            unbindService(gpsServiceConnection);
        } catch (Exception e) {
            Log.e(TAG, "Could not unbind gpsDataCaptureService", e);
        }

        try {
            stopService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not stop gpsDataCaptureService", e);
        }
    }
}
