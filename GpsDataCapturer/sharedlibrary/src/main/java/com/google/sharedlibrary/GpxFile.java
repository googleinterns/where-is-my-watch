package com.google.sharedlibrary;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

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
            FileWriter fileWriter = new FileWriter(gpxFile, true);
            Log.d(TAG, "Writing the xml header");
            fileWriter.write(writeFileHeader(fileName));
            fileWriter.close();

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
        return getFormattedCurrentTime(context);
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
     * Create the xml header with version, creator and metadata
     *
     * @param formattedTime time of on location changed in format
     * @return A header string
     */
    public static String writeFileHeader(String formattedTime) {
        StringBuilder header = new StringBuilder();
        header.append("<?xml version='1.0' encoding='UTF-8' ?>");
        header.append("<gpx version=\"1.1\" creator=\"GpsDataCapturer " + BuildConfig.VERSION_CODE
                + "\" ");
        header.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        header.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        header.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 ");
        header.append("http://www.topografix.com/GPX/1/1/gpx.xsd\">");
        header.append("<metadata><time>").append(formattedTime).append("</time>");
        header.append("<device>").append(Build.DEVICE).append("</device>");
        header.append("<id>").append(Build.ID).append("</id>");
        header.append("<manufacturer>").append(Build.MANUFACTURER).append("</manufacturer>");
        header.append("<model>").append(Build.MODEL).append("</model></metadata>");
        header.append("<trk>");
        header.append("<trkseg>");
        return header.toString();
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
//            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Log.d(TAG, "Writing the xml footer.");
            fileWriter.write(
                    "</trkseg></trk><time>" + getFormattedCurrentTime(context) + "</time></gpx>");
//            bufferedWriter.flush();
//            bufferedWriter.close();
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

    /**
     * Write the data to file
     *
     * @param location the location captured from GPS
     */
    public static void writeToFile(File gpxFile, Context context, Location location) {
        GpxFileWriter gpxFileWriter = new GpxFileWriter(gpxFile, true);
        try {
            Log.d(TAG, "Starting gpx file writer");
            gpxFileWriter.write(context, location);
        } catch (Exception e) {
            Log.e(TAG, "Could not write to file", e);
        }
    }
}