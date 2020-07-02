package com.google.sharedlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;

/**
 * This Utils class wraps all the utility functions
 */
public class Utils {
    private static final String TAG = "System";
    public static final int PERMISSION_REQUEST_CODE = 1;
    public enum ButtonState {START_CAPTURE, STOP_CAPTURE}

    /**
     * Request for all necessary permissions if not granted
     */
    public static void requestNecessaryPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Check if the user granted all the necessary permissions required to run the app
     */
    public static boolean hasUserGrantedNecessaryPermissions(Context context) {
        return hasUserGrantedPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && hasUserGrantedPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                && hasUserGrantedPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasUserGrantedPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Check if the user grant the permission required to run the app
     *
     * @param permissionName the permission name to check
     * @return return true if permission granted and false if not
     */
    private static boolean hasUserGrantedPermission(Context context, String permissionName) {
        boolean granted = ActivityCompat.checkSelfPermission(context, permissionName)
                == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "Permission " + permissionName + " : " + granted);
        return granted;
    }

    /**
     * Toast message asking users to grant the permissions to start the function
     *
     * @param requestCode  the permission request code
     * @param permissions  an array of permission name
     * @param grantResults an array of grant results
     */
    private void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults, Context context) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Required Permissions are not granted.");
                    Toast.makeText(context,
                            "Function disabled, please grant permissions required to run the app",
                            Toast.LENGTH_SHORT).show();
                }
            }
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    /**
     * Check if gps enabled
     *
     * @param locationManager the location manager
     * @return return true if gps provider enabled, false if not
     */
    public static boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Get formatted system time
     *
     * @param context the context
     * @return return a string of formatted current time
     */
    public static String getFormattedCurrentTime(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                context.getResources().getConfiguration().locale);
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * Get Gps data string
     *
     * @param location the update location from gps
     * @return return a gps data string
     */
    public static String getGpsDataString(Location location) {
        StringBuilder gpsDataBuilder = new StringBuilder();
        gpsDataBuilder.append("GPS DATA \n")
                .append("Lat: ").append(location.getLatitude())
                .append("\n")
                .append("Lon: ").append(location.getLongitude())
                .append("\n")
                .append("Speed: ").append(location.getSpeed())
                .append("\n");
        return gpsDataBuilder.toString();
    }
}
