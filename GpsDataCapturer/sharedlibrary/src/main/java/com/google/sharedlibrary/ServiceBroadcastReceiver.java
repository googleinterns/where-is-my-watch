package com.google.sharedlibrary;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.utils.Utils;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "BroadcastReceiver";
    private GpsDataCaptureService gpsDataCaptureService;
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.  During
     * this time you can use the other methods on BroadcastReceiver to view/modify the current
     * result values.  This method is always called within the main thread of its process, unless
     * you explicitly asked for it to be scheduled on a different thread using {@link
     * Context#registerReceiver(BroadcastReceiver, IntentFilter, String, Handler)}. When it runs on
     * the main thread you should never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to be blocked and a
     * candidate to be killed). You cannot launch a popup dialog in your implementation of
     * onReceive().
     *
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this function.</b> This means you
     * should not perform any operations that return a result to you asynchronously. If you need to
     * perform any follow up background work, schedule a {@link JobService} with {@link
     * JobScheduler}.
     * <p>
     * If you wish to interact with a service that is already running and previously bound using
     * {@link Context#bindService(Intent, ServiceConnection, int) bindService()}, you can use {@link
     * #peekService}.
     *
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They are hints to
     * the operating system about how to find suitable recipients. It is possible for senders to
     * force delivery to specific recipients, bypassing filter resolution.  For this reason, {@link
     * #onReceive(Context, Intent) onReceive()} implementations should respond only to known
     * actions, ignoring any unexpected Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received request " + action);

        startAndBindGpsDataCaptureService(context);
        if(action.equals("com.google.sharedlibrary.service.GpsDataCaptureService.STARTCAPTURE")){

//            if(gpsDataCaptureService != null) {
                Log.d(TAG, "Start GPS data capture in background!");
                gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);
//            }
        }else if(action.equals("com.google.sharedlibrary.service.GpsDataCaptureService.STOPCAPTURE")){
//            if(gpsDataCaptureService != null) {
                Log.d(TAG, "Stop GPS data capture in background!");
                gpsDataCaptureService.stopCapture(Utils.LocationApiType.LOCATIONMANAGER);
//            }
        }
    }

    /**
     * Provides connection to GpsDataCaptureService
     */
    public final ServiceConnection gpsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GpsDataCaptureService via broadcast receiver.");
            //get the gpsDataCaptureService
            gpsDataCaptureService =
                    ((GpsDataCaptureService.GpsDataCaptureBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from GpsDataCaptureService via broadcast receiver.");
            gpsDataCaptureService = null;
        }
    };

    /**
     * Bind the activity to GpsDataCaptureService
     */
    private void startAndBindGpsDataCaptureService(Context context) {
        Intent serviceIntent = new Intent(context, GpsDataCaptureService.class);
        //start GpsDataCaptureService
        try {
            Log.d(TAG, "Start Service via broadcast receiver!");
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not start service via broadcast receiver", e);
        }
        //Bind to GpsDataCaptureService
        try {
            Log.d(TAG, "Bind Service via broadcast receiver");
            context.bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.e(TAG, "Could not bind Service via broadcast receiver", e);
        }
    }
}
