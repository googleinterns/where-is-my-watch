package com.google.gpsdatacapturer;

import static com.google.sharedlibrary.utils.Utils.isGpsEnabled;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

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

import com.google.gpsdatacapturer.databinding.ActivityMobileGpsMainBinding;
import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.utils.Utils;
import com.google.sharedlibrary.utils.Utils.ButtonState;
import com.google.sharedlibrary.utils.Utils.LocationApiType;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.model.GpsInfoViewModelFactory;

public class MobileGpsMainActivity extends AppCompatActivity {
    private static final String TAG = "MobileGpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private LocationManager locationManager;
    private static boolean isBound = false;

    private ButtonState startAndStopButtonState = ButtonState.START_CAPTURE;
    private LocationApiType
            locationApiType = LocationApiType.LOCATIONMANAGER;

    private RadioGroup apiRadioGroup;
    private Button startAndStopButton;
    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;
    private TextView satellitesTextView;
    private GpsInfoViewModel gpsInfoViewModel;
    private boolean gpsCaptureStopped;

    private Intent mLaunchIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Binding the layout with view model
        ActivityMobileGpsMainBinding dataBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_mobile_gps_main);
        gpsInfoViewModel = new ViewModelProvider(this,
                new GpsInfoViewModelFactory()).get(GpsInfoViewModel.class);
        dataBinding.setGpsInfoViewModel(gpsInfoViewModel);
        dataBinding.setLifecycleOwner(this);

        //Keep the phone awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Initialize all the necessary variables
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        apiRadioGroup = (RadioGroup) findViewById(R.id.m_raido_group_api);
        startAndStopButton = (Button) findViewById(R.id.m_start_stop_button);
        gpsDataTextView = (TextView) findViewById(R.id.m_text_view_gps_data);
        gpsStatusTextView = (TextView) findViewById(R.id.m_text_view_gps_status);
        satellitesTextView = (TextView) findViewById(R.id.m_text_view_satellites);

        //check and request for all necessary permissions
        if (!Utils.hasUserGrantedNecessaryPermissions(this)) {
            Log.d(TAG,
                    "Missing permission. Requesting all necessary permissions..."
                            + ".");
            Utils.requestNecessaryPermissions(this);
        }
        if (!isGpsEnabled(locationManager)) {
            Log.d(TAG, "Gps is not enabled, start to enable.");
            setGpsEnabled();
        }

        //Choose a location api, hide the radio group and show startAndStopButton
        apiRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            locationApiType = checkedId == R.id.m_radio_button_FLP ?
                    LocationApiType.FUSEDLOCATIONPROVIDERCLIENT
                    : LocationApiType.LOCATIONMANAGER;
        });

        //get the launch intent
        mLaunchIntent = this.getIntent();
        Log.d(TAG, mLaunchIntent.toString());

        //Start the service
        startAndBindGpsDataCaptureService();

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (action != null && isBound){
            Log.d(TAG, "Intent action: " + action);
            //Stop capture if it's stop intent
            if (action.equals(
                    "com.google.gpsdatacapturer.STOP_CAPTURE")) {
                Log.d(TAG, "Stop capture via intent onNewIntent");
                gpsDataCaptureService.stopCapture();
            } else if(action.equals("com.google.gpsdatacapturer.START_CAPTURE")){
                Utils.startCaptureViaIntent(gpsDataCaptureService, intent);
                Log.d(TAG, "Start capture onNewIntent");
            }
        }
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
        if (!gpsCaptureStopped) {
            stopGpsCapture();
        }
        unbindGpsDataCaptureService();
        super.onDestroy();
    }

    /**
     * Set GPS if it's not enabled
     */
    private void setGpsEnabled() {
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
        gpsDataCaptureService.setLocationApiType(locationApiType);
        gpsDataCaptureService.startCapture();
    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void stopGpsCapture() {
        if (!isBound) {
            Log.e(TAG, "GpsDataCaptureService is not bound");
        }

        Log.d(TAG, "Stop capture data.");
        gpsDataCaptureService.stopCapture();
        gpsCaptureStopped = true;
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
    final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GpsDataCaptureService.");
            //get the gpsDataCaptureService
            gpsDataCaptureService =
                    ((GpsDataCaptureService.GpsDataCaptureBinder) service).getService();

            String action = mLaunchIntent.getAction();
            if (action != null && action.equals("com.google.gpsdatacapturer.START_CAPTURE")) {
                //start capture via launch intent
                try {
                    Utils.startCaptureViaIntent(gpsDataCaptureService, mLaunchIntent);
                    Log.d(TAG, "Start capture via launch intent onServiceConnected");
                } catch (Exception e) {
                    Log.d(TAG, "Could not start capture via launch intent");
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
        Intent serviceIntent = new Intent(this, GpsDataCaptureService.class);
        //start GpsDataCaptureService
        try {
            Log.d(TAG, "Start service");
            startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not start gpsDataCaptureService", e);
        }
        //Bind to GpsDataCaptureService
        try {
            Log.d(TAG, "Bind service");
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

        if (locationApiType == LocationApiType.FUSEDLOCATIONPROVIDERCLIENT) {
            Log.d(TAG, "GPS Status not available via FusedLocationProvider");
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
        apiRadioGroup.check(R.id.m_radio_button_LM);
        apiRadioGroup.setVisibility(View.VISIBLE);
        locationApiType = LocationApiType.LOCATIONMANAGER;
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
