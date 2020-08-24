package com.google.sharedlibrary.locationhelper;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

/**
 * Helper class that help the GpsDataCaptureService to start/stop the FusedLocationProviderClient.
 */
public class FusedLocationProviderHelper {
  private static final String TAG = "FLPHelper";
  private static final int INTERVAL = 1000;
  private static final int FASTEST_INTERVAL = 1000;
  private static LocationRequest locationRequest;

  /** Start fusedLocationProviderClient */
  @SuppressLint("MissingPermission")
  public static void startFusedLocationProviderClient(
      FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback) {
    createLocationRequest();

    fusedLocationProviderClient
        .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        .addOnFailureListener(
            (e) -> {
              Log.e(TAG, "FusedLocationProviderClient could not be started.", e);
            })
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "FusedLocationProviderClient request location update completed.");
              }
            });
  }

  /** Create location request */
  private static void createLocationRequest() {
    locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(INTERVAL);
    locationRequest.setFastestInterval(FASTEST_INTERVAL);
  }

  /** Stop fusedLocationProviderClient */
  public static void stopFusedLocationProviderClient(
      FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback) {
    if (fusedLocationProviderClient != null) {
      fusedLocationProviderClient
          .removeLocationUpdates(locationCallback)
          .addOnFailureListener(
              (e) -> {
                Log.e(TAG, "FusedLocationProviderClient could not be removed.", e);
              })
          .addOnCompleteListener(
              new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  Log.d(
                      TAG, "FusedLocationProviderClient removed location updates " + "completed.");
                }
              });
    }
  }
}
