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
    private boolean addNewSegment;
    private static final int SIZE = 20800;
    public GpxWriteHandler(String formattedTime, File gpxFile, Location location, boolean addNewSegment){
        this.formattedTime = formattedTime;
        this.gpxFile = gpxFile;
        this.location = location;
        this.addNewSegment = addNewSegment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        synchronized (GpxFileWriter.lock){
            try{
                FileWriter fileWriter = new FileWriter(gpxFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, SIZE);
                if(!Files.exists(gpxFile.toPath())){
                    try {
                        gpxFile.createNewFile();
                    } catch (IOException e) {
                        Log.d(TAG, "Could not create new file", e);
                    }
                    try {
                        bufferedWriter.write(xmlHeader(formattedTime));
                        bufferedWriter.write("<trk>");
                        bufferedWriter.write(xmlFooter());
                        bufferedWriter.flush();
                        bufferedWriter.close();

                        //New file, so new segment.
                        addNewSegment = true;
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "Could not find gpxFile", e);
                    }
                }
                bufferedWriter.write(getTrackPointXml(location,formattedTime));

                bufferedWriter.flush();
                bufferedWriter.close();

                //Todo: write data to file
                Log.i(TAG, getTrackPointXml(location,formattedTime));
                Log.d(TAG, "Finished writing to GPX file");

            } catch (Exception e) {
                Log.e(TAG,"GpxFileWriter.write", e);
            }
        }
    }

    /**
     * Create the xml header with version, creator and metadata
     * @param formattedTime time of on location changed in format
     * @return header string
     */
    private String xmlHeader(String formattedTime){
        StringBuilder header = new StringBuilder();
        header.append("<?xml version=\"1.1\" encoding=\"UTF-8\" ?>");
        header.append("<gpx version=\"1.1\" creator=\"GpsDataCapturer\" >");
        header.append("<xsd:complexType name=\"metadataType\">");
        //Todo get the metadata: identifier: device/build/execution id/start time/stop time
        header.append("<xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\"/>");
        header.append("xmlns=\"http://www.topografix.com/GPX/1/0\" ");
        header.append("<time>").append(formattedTime).append("</time>");
        return header.toString();
    }
    /**
     * Create the xml footer
     * @return footer string
     */
    private String xmlFooter(){
        return "</trk></gpx>";
    }

    /**
     * Create the xml footer with segment
     * @return footer string
     */
    private String xmlFooterWithSegment(){
            return "</trkseg></trk></gpx>";
        }

    /**
     * Generate the xml segment of the location
     * @param loc the location captured by GPS
     * @param formattedTime  time of on location changed in format
     * @return a string of xml segment
     */
    private String getTrackPointXml(Location loc, String formattedTime) {

        StringBuilder track = new StringBuilder();

        if (addNewSegment) {
            track.append("<trkseg>");
        }

        track.append("<trkpt lat=\"")
                .append(String.valueOf(loc.getLatitude()))
                .append("\" lon=\"")
                .append(String.valueOf(loc.getLongitude()))
                .append("\">");

        if (loc.hasAltitude()) {
            track.append("<ele>").append(String.valueOf(loc.getAltitude())).append("</ele>");
        }

        track.append("<time>").append(formattedTime).append("</time>");

        track.append("<speed>").append(loc.hasSpeed()? loc.getSpeed() : "0.0").append("</speed>");

        track.append("<accuracy>").append(loc.hasAccuracy()? loc.getAccuracy() : "0.0").append("</accuracy>");

        track.append("<src>").append(loc.getProvider()).append("</src>");

        track.append("</trkpt>\n");

        track.append("</trkseg></trk></gpx>");

        return track.toString();
    }
}
