package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.sharedlibrary.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * This class deals everything related to the file and file folder. It provides new folder creation,
 * new file creation, and writing header/captured data/footer etc., functions.
 */
public class GpxFileHelper {
    private static final String TAG = "GpxFile";

    /**
     * Create a new gpxFile
     *
     * @param gpxDataFolder the folder to store the new file
     * @param fileName      the name of new file
     * @return return new created file
     */
    public static File createGpxFile(File gpxDataFolder, String fileName) {
        Log.i(TAG, "Create a new gpxFile " + fileName);
        File gpxFile = new File(gpxDataFolder.getPath(), fileName + ".xml");
        try {
            gpxFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Could not create new gpx file.", e);
        }
        return gpxFile;
    }

    /**
     * Get the name for new file
     *
     * @param context the context
     * @return return the name for new file
     */
    public static String getNewFileName(Context context) {
        return Utils.getFormattedCurrentTime(context);
    }

    /**
     * Reset the gpxFile to null
     *
     * @param gpxFile the gpxFile
     */
    public static void resetGpxFile(File gpxFile) {
        gpxFile = null;
    }
}