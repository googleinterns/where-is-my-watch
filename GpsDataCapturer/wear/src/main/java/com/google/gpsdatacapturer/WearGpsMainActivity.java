package com.google.gpsdatacapturer;

import static com.google.sharedlibrary.utils.Utils.isGpsEnabled;

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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.wear.ambient.AmbientModeSupport;

import com.google.gpsdatacapturer.databinding.ActivityWearGpsMainBinding;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.model.GpsInfoViewModelFactory;
import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.utils.Utils;
import com.google.sharedlibrary.utils.Utils.ButtonState;
import com.google.sharedlibrary.utils.Utils.LocationApiType;

public class WearGpsMainActivity extends AppCompatActivity implements
        AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = "WearGpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private LocationManager locationManager;
    private static boolean isBound = false;

    private ButtonState startAndStopButtonState = ButtonState.START_CAPTURE;
    private LocationApiType locationApiType = LocationApiType.LOCATIONMANAGER;

    private RadioGroup apiRadioGroup;
    private Button startAndStopButton;
    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;
    private TextView satellitesTextView;
    private GpsInfoViewModel gpsInfoViewModel;
    private boolean gpsCaptureStopped;

    private Intent adbIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the action bar
        getSupportActionBar().hide();

        //Binding the layout with view model
        ActivityWearGpsMainBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_wear_gps_main);
        gpsInfoViewModel = new ViewModelProvider(this,
                new GpsInfoViewModelFactory()).get(GpsInfoViewModel.class);
        binding.setGpsInfoViewModel(gpsInfoViewModel);
        binding.setLifecycleOwner(this);


        //Keep the device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Initialize all the necessary variables
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        apiRadioGroup = (RadioGroup) findViewById(R.id.radio_group_location_api);
        startAndStopButton = (Button) findViewById(R.id.button_start_stop);
        gpsDataTextView = (TextView) findViewById(R.id.text_view_gps_data);
        gpsStatusTextView = (TextView) findViewById(R.id.text_view_gps_status);
        satellitesTextView = (TextView) findViewById(R.id.text_view_satellites);

        //check and request for all necessary permissions
        if (!Utils.hasUserGrantedNecessaryPermissions(this)) {
            Utils.requestNecessaryPermissions(this);
        }

        if (!Utils.isGpsEnabled(locationManager)) {
            setGpsEnabled();
        }

        //Choose a location api, hide the radio group and show startAndStopButton
        apiRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            locationApiType = checkedId == R.id.radio_button_FLP ?
                    LocationApiType.FUSEDLOCATIONPROVIDERCLIENT
                    : LocationApiType.LOCATIONMANAGER;
        });

        //Start the service
        startAndBindGpsDataCaptureService();

        //get the called intent
        adbIntent = this.getIntent();
        Log.d(TAG, adbIntent.toString());

        //start and bind service on start button clicked and start capture once the service is
        // connected
        //stop capture data, then stop and unbind service on stop button clicked
        startAndStopButton.setOnClickListener((View v) -> {
            if (startAndStopButtonState == ButtonState.START_CAPTURE) {
                //hide radio group
                apiRadioGroup.setVisibility(View.GONE);

                showGpsDataAndStatusTextView();

                startGpsCapture();

                switchToStopButton();
            } else {
                stopGpsCapture();

                hideGpsDataAndStatusTextView();

                resetRadioGroup();

                switchToStartButton();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "On Resume Gps enabled: " + isGpsEnabled(locationManager));
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(!gpsCaptureStopped) {
            stopGpsCapture();
        }
        unbindGpsDataCaptureService();
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
    public void startGpsCapture() {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound, could not start capture.");
            return;
        }

        if (!isGpsEnabled(locationManager)) {
            Log.e(TAG, "GPS provider is not enabled, could not start capture.");
            return;
        }

        Log.d(TAG, "Start capture data.");
        gpsDataCaptureService.setGpsInfoViewModel(gpsInfoViewModel);
        gpsDataCaptureService.startCapture(locationApiType);
    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void stopGpsCapture() {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound");
        }

        Log.d(TAG, "Stop capture data.");
        gpsDataCaptureService.stopCapture(locationApiType);
        gpsCaptureStopped = true;
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
     public final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GpsDataCaptureService.");
            //get the gpsDataCaptureService
            gpsDataCaptureService =
                    ((GpsDataCaptureService.GpsDataCaptureBinder) service).getService();

            //start capture via intent
            if(adbIntent != null) {
                //Extra the location api type
                if(adbIntent.getExtras() != null){
                    Log.d(TAG, "Intent extra is " + adbIntent.getExtras().toString());
                    boolean type_from_intent = adbIntent.getBooleanExtra("fused_location_type", false);
                    Log.d(TAG, "fused_location_type: " + type_from_intent);
                    if(type_from_intent){
                        locationApiType = LocationApiType.FUSEDLOCATIONPROVIDERCLIENT;
                    }
                    Log.d(TAG, "LocationApiType: " + locationApiType);
                }
                //Start capture if the action is START_CAPTURE
                if (adbIntent.getAction() != null) {
                    Log.d(TAG, "Intent action: " + adbIntent.getAction());
                    if (adbIntent.getAction().equals(
                            "com.google.gpsdatacapturer.START_CAPTURE")) {
                        Log.d(TAG, "Start capture via intent");
                        gpsDataCaptureService.startCapture(locationApiType);
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from GpsDataCaptureService");
            gpsDataCaptureService = null;
        }
    };

    /**
     * Bind the activity to GpsDataCaptureService
     */
    private void startAndBindGpsDataCaptureService() {
        serviceIntent = new Intent(this, GpsDataCaptureService.class);
        //start GpsDataCaptureService
        try {
            Log.d(TAG, "Start Service");
            startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not start gpsDataCaptureService", e);
        }
        //Bind to GpsDataCaptureService
        try {
            Log.d(TAG, "Bind Service");
            isBound = bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Could not bind gpsDataCaptureService", e);
        }
    }

    /**
     * Unbind the activity from GpsDataCaptureService
     */
    private void unbindGpsDataCaptureService() {
        //Unbind from GpsDataCaptureService
        try {
            if (isBound) {
                unbindService(gpsServiceConnection);
                isBound = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not unbind gpsDataCaptureService", e);
        }
    }

    /**
     * Show gps data and status text view
     */
    private void showGpsDataAndStatusTextView() {
        gpsDataTextView.setVisibility(View.VISIBLE);
        gpsStatusTextView.setVisibility(View.VISIBLE);
        satellitesTextView.setVisibility(View.VISIBLE);

        if(locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT){
            gpsStatusTextView.setText(R.string.gps_status_not_available);
            satellitesTextView.setText(R.string.satellites_not_available);
        }
    }

    /**
     * Hide gps data and status text view
     */
    private void hideGpsDataAndStatusTextView() {
        gpsDataTextView.setVisibility(View.GONE);
        gpsStatusTextView.setVisibility(View.GONE);
        satellitesTextView.setVisibility(View.GONE);
    }

    /**
     * Reset radio group to initial state
     */
    private void resetRadioGroup() {
        apiRadioGroup.check(R.id.radio_button_LM);
        apiRadioGroup.setVisibility(View.VISIBLE);
    }

    /**
     * Switch to stop button
     */
    private void switchToStopButton() {
        startAndStopButton.setText(R.string.stop_capture);
        startAndStopButtonState = ButtonState.STOP_CAPTURE;
        startAndStopButton.setBackground(
                getResources().getDrawable(R.drawable.wear_button_red));
    }

    /**
     * Switch to start button
     */
    private void switchToStartButton() {
        startAndStopButton.setText(R.string.start_capture);
        startAndStopButtonState = ButtonState.START_CAPTURE;
        startAndStopButton.setBackground(
                getResources().getDrawable(R.drawable.wear_button_green));
    }

    /**
     * @return the {@link AmbientModeSupport.AmbientCallback} to be used by this class to
     * communicate with the entity interested in ambient events.
     */
    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private static class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            // Handle entering ambient mode
            super.onEnterAmbient(ambientDetails);
        }

        @Override
        public void onExitAmbient() {
            // Handle exiting ambient mode
            super.onExitAmbient();
        }

        @Override
        public void onUpdateAmbient() {
            // Update the content
            super.onUpdateAmbient();
        }
    }
}