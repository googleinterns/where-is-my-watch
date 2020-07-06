package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.google.sharedlibrary.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class deals everything related to the file and file folder. It provides new folder creation,
 * new file creation, and writing header/captured data/footer etc., functions.
 */
public class GpxFile {
    private static String TAG = "GpxFile";

    /**
     * Create GpsDataFolder if not exist.
     *
     * @param context the context
     * @return return the folder if not created
     */
    public static File createGpsDataFolder(Context context) {
        File gpxDataFolder = context.getExternalFilesDir(
                Environment.getExternalStorageDirectory().getAbsolutePath());
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
     * Write the data to file
     *
     * @param location the location captured from GPS
     */
    public static void writeToFile(File gpxFile, Context context, Location location,
            boolean isNewFile) {
        GpxFileWriter gpxFileWriter = new GpxFileWriter(gpxFile, true);
        try {
            Log.d(TAG, "Starting gpx file writer");
            gpxFileWriter.write(context, location, isNewFile);
        } catch (Exception e) {
            Log.e(TAG, "Could not write to file", e);
        }
    }

    /**
     * Write the gpx file footer in xml format
     *
     * @param gpxFile the gpxFile
     * @param context the context
     */
    public static void writeFileFooter(File gpxFile, Context context) {
        try {
            FileWriter fileWriter = new FileWriter(gpxFile, true);
            Log.d(TAG, "Writing the xml footer.");
            fileWriter.write(
                    "</trkseg></trk><time>" + Utils.getFormattedCurrentTime(context) + "</time></gpx>");
            fileWriter.close();
            Log.i(TAG, "Finished writing to GPX file");
        } catch (IOException e) {
            Log.e(TAG, "Could not write the xml footer.", e);
        }
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