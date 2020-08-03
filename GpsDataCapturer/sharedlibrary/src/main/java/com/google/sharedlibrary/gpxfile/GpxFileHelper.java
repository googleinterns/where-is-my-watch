package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.sharedlibrary.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Wrapper class for create new GpxFile
 */
public class GpxFileHelper {
    private static final String TAG = "GpxFileHelper";

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
     * Create a new file name
     */
    public static String createFileName(){
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        return dataFormat.format(System.currentTimeMillis());
    }

}