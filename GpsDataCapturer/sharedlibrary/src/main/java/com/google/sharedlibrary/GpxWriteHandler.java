package com.google.sharedlibrary;

import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

public class GpxWriteHandler implements Runnable {
    private final String TAG = "GpxWriterHandler";
    private String formattedTime;
    private Location location;
    private File gpxFile = null;
    private static final int SIZE = 20480;
    public GpxWriteHandler(String formattedTime, File gpxFile, Location location){
        this.formattedTime = formattedTime;
        this.gpxFile = gpxFile;
        this.location = location;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        synchronized (GpxFileWriter.lock){
            Log.i(TAG, "Start writing to file");
            try{
                FileWriter fileWriter = new FileWriter(gpxFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, SIZE);

                bufferedWriter.write(getTrackPointXml(location, formattedTime));
                bufferedWriter.flush();
                bufferedWriter.close();

                Log.i(TAG, getTrackPointXml(location,formattedTime));
                Log.d(TAG, "Finished writing to GPX file");

            } catch (Exception e) {
                Log.e(TAG,"GpxFileWriter.write", e);
            }
        }
    }

    /**
     * Generate the xml segment of the location
     * @param loc the location captured by GPS
     * @param formattedTime  time of on location changed in format
     * @return a string of xml segment
     */
    private String getTrackPointXml(Location loc, String formattedTime) {

        StringBuilder trackPoint = new StringBuilder();

        trackPoint.append("<trkpt lat=\"")
                .append(String.valueOf(loc.getLatitude()))
                .append("\" lon=\"")
                .append(String.valueOf(loc.getLongitude()))
                .append("\">");

        if (loc.hasAltitude()) {
            trackPoint.append("<ele>").append(String.valueOf(loc.getAltitude())).append("</ele>");
        }

        trackPoint.append("<time>").append(formattedTime).append("</time>");

        trackPoint.append("<speed>").append(loc.hasSpeed()? loc.getSpeed() : "0.0").append("</speed>");

        trackPoint.append("<accuracy>").append(loc.hasAccuracy()? loc.getAccuracy() : "0.0").append("</accuracy>");

        trackPoint.append("<src>").append(loc.getProvider()).append("</src>");

        trackPoint.append("</trkpt>\n");

        return trackPoint.toString();
    }
}
