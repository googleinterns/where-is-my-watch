package com.google.gpsdatacapturer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;

import com.google.sharedlibrary.GpsDataCaptureService;
import com.google.sharedlibrary.GpsDataCaptureService.GpsDataCaptureBinder;

public class GpsMainActivity extends WearableActivity {
    private static final String TAG = "GpsMainActivity";
    private static GpsDataCaptureService gpsDataCaptureService;
    private static Intent serviceIntent;
    private static boolean isBound = false;
    private static final int REQUEST_CODE = 1340;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_main);

        requestPermissionsIfNotGranted();

        startAndBindGpsDataCaptureService();

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
        startAndBindGpsDataCaptureService();
    }

    @Override
    protected void onStop(){
        super.onStop();
        stopAndUnbindGpsDataCaptureService();
        isBound = false;
    }

    private void  requestPermissionsIfNotGranted(){
        boolean granted = ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!granted){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch(requestCode){
            case REQUEST_CODE:{
                if(grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "ACCESS_FINE_LOCATION permission is not granted.");
                }
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    /**
     * On click of Start button, start capturing gps data
     */
    public void onStartButtonClick(View view){
        if(!isBound){
            Log.i(TAG, "GpsDataCaptureService is not bound, could not start capture.");
            return;
        }
        Log.i(TAG, "Start capture data.");
        gpsDataCaptureService.startCapture();

    }

    /**
     * On click of Stop button, stop capturing gps data
     */
    public void onStopButtonClick(View view){
        if(!isBound){
            Log.i(TAG, "GpsDataCaptureService is not bound");
        }
        Log.i(TAG, "Stop capture data.");
        gpsDataCaptureService.stopCapture();
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
    private void startAndBindGpsDataCaptureService(){
        serviceIntent = new Intent(this, GpsDataCaptureService.class);
        //start GpsDataCaptureService
        try{
            startService(serviceIntent);
        }catch (Exception e){
            Log.e(TAG, "Could not start gpsDataCaptureService", e);
        }
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
    private void stopAndUnbindGpsDataCaptureService(){
        //Unbind from GpsDataCaptureService
        try {
            unbindService(gpsServiceConnection);
        }catch (Exception e){
            Log.e(TAG, "Could not unbind gpsDataCaptureService", e);
        }

        try{
            stopService(serviceIntent);
        }catch (Exception e){
            Log.e(TAG, "Could not stop gpsDataCaptureService", e);
        }
    }
}
