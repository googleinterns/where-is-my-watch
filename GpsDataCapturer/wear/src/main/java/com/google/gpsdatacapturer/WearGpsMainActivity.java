package com.google.gpsdatacapturer;

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
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.sharedlibrary.GpsDataCaptureService;
import com.google.sharedlibrary.GpsDataCaptureService.GpsDataCaptureBinder;
import com.google.sharedlibrary.GpsDataCaptureService.LocationApiVersion;

public class WearGpsMainActivity extends WearableActivity {
    private static final String TAG = "WearGpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private LocationManager locationManager;
    private static boolean isBound = false;
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int READ_WRITE_REQUEST_CODE = 2;
    private boolean isGpsEnabled = false;

    public static enum ButtonState {START_CAPTURE, STOP_CAPTURE}

    ;
    private ButtonState startAndStopButtonState = ButtonState.START_CAPTURE;
    private LocationApiVersion apiVersion;

    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_gps_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsDataTextView = (TextView) findViewById(R.id.text_view_gps_data);
        gpsStatusTextView = (TextView) findViewById(R.id.text_view_gps_status);

        requestLocationPermissionsIfNotGranted();
        requestReadWritePermissionsIfNotGranted();

        setGpsIfNotEnabled();

        //Choose a location api, hide the radio group and show startAndStopButton
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group_location_api);
        final Button startAndStopButton = (Button) findViewById(R.id.button_start_stop);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_FPL) {
                    apiVersion = LocationApiVersion.FUSEDLOCATIONPROVIDERCLIENT;
                } else if (checkedId == R.id.radio_button_LM) {
                    apiVersion = LocationApiVersion.LOCATIONMANAGER;
                }
                radioGroup.setVisibility(View.GONE);
                startAndStopButton.setVisibility(View.VISIBLE);

            }
        });

        //start and bind the service
        startAndBindGpsDataCaptureService();

        //start capture data if the button state is START_CAPTURE, and stop if the state is
        // STOP_CAPTURE
        startAndStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startAndStopButtonState == ButtonState.START_CAPTURE) {
                    //show gpsDataTextView
                    gpsDataTextView.setVisibility(View.VISIBLE);
                    gpsStatusTextView.setVisibility(View.VISIBLE);

                    //start capture gps data
                    startCapture(apiVersion);

                    //switch button to stop state
                    startAndStopButton.setText(R.string.stop_capture);
                    startAndStopButtonState = ButtonState.STOP_CAPTURE;
                    startAndStopButton.setBackground(
                            getResources().getDrawable(R.drawable.wear_button_red));
                } else {
                    //stop capture gps data
                    stopCapture(apiVersion);

                    //hide gpsDataTextView
                    gpsDataTextView.setVisibility(View.GONE);
                    gpsStatusTextView.setVisibility(View.GONE);

                    //reset radioGroup
                    radioGroup.clearCheck();
                    radioGroup.setVisibility(View.VISIBLE);

                    //reset startAndStopButton
                    startAndStopButton.setText(R.string.start_capture);
                    startAndStopButtonState = ButtonState.START_CAPTURE;
                    startAndStopButton.setBackground(
                            getResources().getDrawable(R.drawable.wear_button_green));
                    startAndStopButton.setVisibility(View.GONE);
                }
            }
        });

        // Enables Always-on
        setAmbientEnabled();
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
    private void setGpsIfNotEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
    }

    /**
     * On click of Start button, start capturing gps data
     */
    public void startCapture(LocationApiVersion apiVersion) {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound, could not start capture.");
            return;
        }
        Log.d(TAG, "Start capture data.");
        gpsDataCaptureService.startCapture(apiVersion, gpsDataTextView, gpsStatusTextView);
    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void stopCapture(LocationApiVersion apiVersion) {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound");
        }
        Log.d(TAG, "Stop capture data.");
        gpsDataCaptureService.stopCapture(apiVersion);
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GpsDataCaptureService.");
            GpsDataCaptureBinder binder = (GpsDataCaptureBinder) service;
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
        //Stop GpsDataCaptureService
        try {
            stopService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not stop gpsDataCaptureService", e);
        }
    }
}
