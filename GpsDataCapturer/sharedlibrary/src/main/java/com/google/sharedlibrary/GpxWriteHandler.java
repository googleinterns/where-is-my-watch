package com.google.sharedlibrary;

import android.content.Context;
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
    private static final String TAG = "GpxWriterHandler";
    private final String formattedTime;
    private final Location location;
    private final File gpxFile;
    private final Context context;
    private final boolean append;
    private boolean isNewFile;
    private static final int SIZE = 20480;


    public GpxWriteHandler(Context context, String formattedTime, File gpxFile,
            Location location, boolean append, boolean isNewFile) {
        this.context = context;
        this.formattedTime = formattedTime;
        this.gpxFile = gpxFile;
        this.location = location;
        this.append = append;
        this.isNewFile = isNewFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        Log.i(TAG, "Start writing to file");
        try (FileWriter fileWriter = new FileWriter(gpxFile, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, SIZE);

            //Write the header if isNewFile
            if (isNewFile) {
                Log.d(TAG, "Writing new file header.");
                bufferedWriter.write(createFileHeader(Utils.getFormattedCurrentTime(context)));
            }

            //write the captured gps data to file
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
     * Create the xml header with version, creator and metadata
     *
     * @param formattedStartTime time of on location changed in format
     * @return A header string
     */
    public static String createFileHeader(String formattedStartTime) {
        StringBuilder header = new StringBuilder();

        header.append("<?xml version='1.0' encoding='UTF-8' ?>");
        header.append("<gpx version=\"1.1\" creator=\"GpsDataCapturer " + BuildConfig.VERSION_CODE
                + "\" ");
        header.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        header.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        header.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 ");
        header.append("http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
        header.append("<metadata><time>").append(formattedStartTime).append("</time>");
        header.append("<device>").append(Build.DEVICE).append("</device>");
        header.append("<id>").append(Build.ID).append("</id>");
        header.append("<manufacturer>").append(Build.MANUFACTURER).append("</manufacturer>");
        header.append("<model>").append(Build.MODEL).append("</model></metadata>\n");
        header.append("<trk>\n");
        header.append("<trkseg>\n");
        return header.toString();
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
