package com.google.gpsdatacapturer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.sharedlibrary.GpsDataCaptureService;
import com.google.sharedlibrary.Utils;
import com.google.sharedlibrary.Utils.ButtonState;

public class MobileGpsMainActivity extends AppCompatActivity {
    private static final String TAG = "MobileGpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private LocationManager locationManager;
    private static boolean isBound = false;

    private ButtonState startAndStopButtonState = ButtonState.START_CAPTURE;
    private GpsDataCaptureService.LocationApiVersion
            locationApiVersion = GpsDataCaptureService.LocationApiVersion.LOCATIONMANAGER;

    private RadioGroup apiRadioGroup;
    private Button startAndStopButton;
    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_gps_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        apiRadioGroup = (RadioGroup) findViewById(R.id.m_raido_group_apiversion);
        startAndStopButton = (Button) findViewById(R.id.m_start_stop_button);
        gpsDataTextView = (TextView) findViewById(R.id.m_text_view_gps_data);
        gpsStatusTextView = (TextView) findViewById(R.id.m_text_view_gps_status);

        //check and request for all necessary permissions
        if (!Utils.hasUserGrantedNecessaryPermissions(this)) {
            Utils.requestNecessaryPermissions(this);
        }

        if (!Utils.isGpsEnabled(locationManager)) {
            setGpsEnabled();
        }

        //Choose a location api, hide the radio group and show startAndStopButton
        apiRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            locationApiVersion = checkedId == R.id.m_radio_button_FLP ?
                    GpsDataCaptureService.LocationApiVersion.FUSEDLOCATIONPROVIDERCLIENT
                    : GpsDataCaptureService.LocationApiVersion.LOCATIONMANAGER;
        });

        //start and bind the service
        startAndBindGpsDataCaptureService();

        //start capture data if the button state is START_CAPTURE, and stop if the state is
        // STOP_CAPTURE
        startAndStopButton.setOnClickListener((View v) -> {
            if (startAndStopButtonState == Utils.ButtonState.START_CAPTURE) {
                //hide radio group
                apiRadioGroup.setVisibility(View.GONE);

                showGpsDataAndStatusTextView();

                startGpsCapture(locationApiVersion);

                switchToStopButton();
            } else {
                stopGpsCapture(locationApiVersion);

                hideGpsDataAndStatusTextView();

                resetRadioGroup();

                switchToStartButton();
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

    @Override
    protected void onResume() {
        super.onResume();
        startAndBindGpsDataCaptureService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAndUnbindGpsDataCaptureService();
        isBound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Set GPS if it's not enabled
     */
    private void setGpsEnabled() {
        //Todo pop out a dialog to accept/deny enable setting??
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    /**
     * On click of Start button, start capturing gps data
     */
    public void startGpsCapture(GpsDataCaptureService.LocationApiVersion apiVersion) {
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
    public void stopGpsCapture(GpsDataCaptureService.LocationApiVersion apiVersion) {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound");
        }
        if (!Utils.isGpsEnabled(locationManager)) {
            Log.e(TAG, "GPS provider is not enabled");
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
            isBound = bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
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
            if (isBound) {
                unbindService(gpsServiceConnection);
            }
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

    /**
     * Show gps data and status text view
     */
    private void showGpsDataAndStatusTextView() {
        gpsDataTextView.setVisibility(View.VISIBLE);
        gpsStatusTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Hide gps data and status text view
     */
    private void hideGpsDataAndStatusTextView() {
        gpsDataTextView.setVisibility(View.GONE);
        gpsStatusTextView.setVisibility(View.GONE);
    }

    /**
     * Reset radio group to initial state
     */
    private void resetRadioGroup() {
        apiRadioGroup.clearCheck();
        apiRadioGroup.setVisibility(View.VISIBLE);
    }

    /**
     * Switch to stop button
     */
    private void switchToStopButton() {
        startAndStopButton.setText(R.string.stop_capture);
        startAndStopButtonState = ButtonState.STOP_CAPTURE;
        startAndStopButton.setBackground(
                getResources().getDrawable(R.drawable.mobile_button_red));
    }

    /**
     * Switch to start button
     */
    private void switchToStartButton() {
        startAndStopButton.setText(R.string.start_capture);
        startAndStopButtonState = ButtonState.START_CAPTURE;
        startAndStopButton.setBackground(
                getResources().getDrawable(R.drawable.mobile_button_green));
    }
}
