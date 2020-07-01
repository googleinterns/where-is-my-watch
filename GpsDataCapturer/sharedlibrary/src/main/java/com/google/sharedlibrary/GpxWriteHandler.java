package com.google.sharedlibrary;

import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Helper class that handle data writing for the GpxFileWriter.
 */
public class GpxWriteHandler implements Runnable {
    private final String TAG = "GpxWriterHandler";
    private final String formattedTime;
    private final Location location;
    private final File gpxFile;
    private final boolean append;
    private static final int SIZE = 20480;


    public GpxWriteHandler(String formattedTime, File gpxFile, Location location, boolean append) {
        this.formattedTime = formattedTime;
        this.gpxFile = gpxFile;
        this.location = location;
        this.append = append;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        Log.i(TAG, "Start writing to file");
        try (FileWriter fileWriter = new FileWriter(gpxFile, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, SIZE);

            bufferedWriter.write(getTrackPointXml(location, formattedTime));
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
            Log.d(TAG, getTrackPointXml(location, formattedTime));
        } catch (Exception e) {
            Log.e(TAG, "GpxFileWriter.write", e);
        }
    }

    /**
     * Generate the xml track point of the location
     *
     * @param location      the location captured by GPS
     * @param formattedTime time of on location changed in format
     * @return a string of xml track point
     */
    private String getTrackPointXml(Location location, String formattedTime) {

        StringBuilder trackPoint = new StringBuilder();

        trackPoint.append("<trkpt lat=\"")
                .append(location.getLatitude())
                .append("\" lon=\"")
                .append(location.getLongitude())
                .append("\">");

        if (location.hasAltitude()) {
            trackPoint.append("<ele>").append(location.getAltitude()).append("</ele>");
        }

        trackPoint.append("<time>").append(formattedTime).append("</time>");

        trackPoint.append("<speed>").append(
                location.hasSpeed() ? location.getSpeed() : "0.0").append("</speed>");

        trackPoint.append("<accuracy>").append(
                location.hasAccuracy() ? location.getAccuracy() : "0.0").append("</accuracy>");

        trackPoint.append("<src>").append(location.getProvider()).append("</src>");

        trackPoint.append("</trkpt>\n");

        return trackPoint.toString();
    }
}
