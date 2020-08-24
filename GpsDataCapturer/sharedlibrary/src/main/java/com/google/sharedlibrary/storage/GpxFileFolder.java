package com.google.sharedlibrary.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class GpxFileFolder {
    private static final String TAG ="GpxFileFolder";
    /**
     * Create GpsDataFolder if not exist.
     *
     * @param context the context
     * @return return the folder if not created
     */
    public static File createGpsDataFolder(Context context) {
    File gpxDataFolder =
        new File(
            context.getExternalFilesDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                + File.separator
                + "GpxDataFolder");
        try {
            assert gpxDataFolder != null;
            if (!gpxDataFolder.exists()) {
                gpxDataFolder.mkdir();
            }
            Log.i(TAG, "Create GpsDataFolder path" + gpxDataFolder.getPath());
        } catch (Exception e) {
            Log.e(TAG, "Could not create new folder.", e);
        }
        return gpxDataFolder;
    }
}
