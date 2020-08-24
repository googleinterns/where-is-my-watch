package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.sharedlibrary.BuildConfig;
import com.google.sharedlibrary.model.SatelliteSignalData;
import com.google.sharedlibrary.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/** Helper class that handle data writing for the GpxFileWriter. */
public class GpxWriteHandler implements Runnable {
  private static final String TAG = "GpxWriterHandler";
  private final String formattedTime;
  private final Location location;
  private final SatelliteSignalData signalData;
  private final File gpxFile;
  private final boolean append;
  private static final int SIZE = 20480;

  public GpxWriteHandler(
      String formattedTime,
      File gpxFile,
      Location location,
      SatelliteSignalData signalData,
      boolean append) {
    this.formattedTime = formattedTime;
    this.gpxFile = gpxFile;
    this.location = location;
    this.signalData = signalData;
    this.append = append;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void run() {
    Log.i(TAG, "Start writing to file");
    try (FileWriter fileWriter = new FileWriter(gpxFile, true)) {
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, SIZE);

      // write the captured gps data to file
      String trackPointXml = getTrackPointXml(location, formattedTime, signalData);
      bufferedWriter.write(trackPointXml);
      bufferedWriter.flush();
      bufferedWriter.close();
      fileWriter.close();
      Log.d(TAG, trackPointXml);
    } catch (Exception e) {
      Log.e(TAG, "GpxFileWriter.writeGpsData", e);
    }
  }

  /**
   * Generate the xml track point of the location
   *
   * @param location the location captured by GPS
   * @param formattedTime time of on location changed in format
   * @return a string of xml track point
   */
  private String getTrackPointXml(
      Location location, String formattedTime, SatelliteSignalData signalData) {
    StringBuilder trackPoint = new StringBuilder();

    trackPoint
        .append("<trkpt lat=\"")
        .append(location.getLatitude())
        .append("\" lon=\"")
        .append(location.getLongitude())
        .append("\">");

    if (location.hasAltitude()) {
      trackPoint.append("<ele>").append(location.getAltitude()).append("</ele>");
    }

    trackPoint.append("<time>").append(formattedTime).append("</time>");

    trackPoint
        .append("<speed>")
        .append(location.hasSpeed() ? location.getSpeed() : "0.0")
        .append("</speed>");

    trackPoint
        .append("<accuracy>")
        .append(location.hasAccuracy() ? location.getAccuracy() : "0.0")
        .append("</accuracy>");

    trackPoint.append("<src>").append(location.getProvider()).append("</src>");

    // append satellites used in fix
    if (location.getExtras() != null) {
      int sat = location.getExtras().getInt("satellites", 0);
      trackPoint.append("<sat>").append(sat).append("</sat>");
    }

    // append top 4 satellites signal data
    trackPoint.append("<signal01>").append(signalData.getFirstSignal()).append("</signal01>");
    trackPoint.append("<signal02>").append(signalData.getSecondSignal()).append("</signal02>");
    trackPoint.append("<signal03>").append(signalData.getThirdSignal()).append("</signal03>");
    trackPoint.append("<signal04>").append(signalData.getForthSignal()).append("</signal04>");
    trackPoint.append("<average>").append(signalData.getAverageSignal()).append("</average>");

    trackPoint.append("</trkpt>\n");

    return trackPoint.toString();
  }
}
