package com.google.sharedlibrary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class GpxFile {
    private static String TAG = "GpxFile";

    /**
     * Create GpsDataFolder if not exist.
     */
    public static File createGpsDataFolder(Context context) {
        File gpxDataFolder = context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath());
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
     * Create gpxFile if not exist
     */
    public static File createGpxFile(File gpxDataFolder, String fileName) {
        Log.i(TAG, "Create a new gpxFile " + fileName);
        File gpxFile = new File(gpxDataFolder.getPath(), fileName + ".xml");
        try {
            gpxFile.createNewFile();
            FileWriter fileWriter = new FileWriter(gpxFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Log.d(TAG, "Writing the xml header");
            bufferedWriter.write(GpxFileWriter.xmlHeader(fileName));
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();

        } catch (IOException e) {
            Log.e(TAG,"Could not create new gpx file.", e);
        }
        return gpxFile;
    }

    /**
     * Set the current file name in date format
     */
    public static String getNewFileName(Context context) {
        return getFormattedCurrentTime(context);
    }

    /**
     * Get formatted time
     */
    public static String getFormattedCurrentTime(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", context.getResources().getConfiguration().locale);
        return sdf.format(System.currentTimeMillis());
    }

    public static void writeFileFooter(File gpxFile, Context context){
        try {
            FileWriter fileWriter = new FileWriter(gpxFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Log.d(TAG, "Writing the xml footer.");
            bufferedWriter.write("</trkseg></trk><time>" + getFormattedCurrentTime(context) + "</time></gpx>");
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
            Log.i(TAG, "Finished writing to GPX file");
        } catch (IOException e) {
            Log.e(TAG, "Could not write the xml footer.", e);
        }
    }
}
