package com.google.gpsdatacapturer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.sharedlibrary.GpsDataCaptureService;
import com.google.sharedlibrary.GpsDataCaptureService.GpsDataCaptureBinder;

public class GpsMainActivity extends WearableActivity {
    private static final String TAG = "GpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private static boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_main);

        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartButtonClick(v);
            }
        });

        Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onStopButtonClick(v);
            }
        });
        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onStart(){
        super.onStart();
        bindGpsDataCaptureService();
    }

    @Override
    protected void onStop(){
        super.onStop();
        unbindGpsDataCaptureService();
        isBound = false;
    }

    /**
     * On click of Start button, start capturing gps data
     */
    public void onStartButtonClick(View view){
        if(isBound){
            gpsDataCaptureService.startCapture();
        }
    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void onStopButtonClick(View view){
        if(isBound){
            gpsDataCaptureService.stopCapture();
        }
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"Connected to GpsDataCaptureService.");
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
    private void bindGpsDataCaptureService(){
        serviceIntent = new Intent(this, GpsDataCaptureService.class);
        //Bind to GpsDataCaptureService
        try {
            bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){
            Log.e(TAG, "Could not bind gpsDataCaptureService", e);
        }
    }

    /**
     * Unbind the activity from GpsDataCaptureService
     */
    private void unbindGpsDataCaptureService(){
        //Unbind from GpsDataCaptureService
        try {
            unbindService(gpsServiceConnection);
        }catch (Exception e){
            Log.e(TAG, "Could not unbind gpsDataCaptureService", e);
        }
    }
}
