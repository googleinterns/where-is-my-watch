package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.sharedlibrary.BuildConfig;
import com.google.sharedlibrary.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

public class GpxAnnotationHandler implements Runnable {
    private static final String TAG = "GpxAnnotationHandler";
    private SimpleDateFormat sdf;
    private final File gpxFile;
    private final boolean append;
    private final boolean isHeader;

    public GpxAnnotationHandler(SimpleDateFormat sdf, File gpxFile, boolean append, boolean isHeader) {
        this.sdf = sdf;
        this.gpxFile = gpxFile;
        this.append = append;
        this.isHeader = isHeader;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        try (FileWriter fileWriter = new FileWriter(gpxFile, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            if (isHeader) {
                Log.d(TAG, "Writing new file header.");
                bufferedWriter.write(createFileHeader(sdf.format(System.currentTimeMillis())));
            } else {
                Log.d(TAG, "Writing the xml footer.");
                bufferedWriter.write(createFileFooter());
            }
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
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
    private String createFileHeader(String formattedStartTime) {
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
     * Create the xml footer
     *
     * @return A footer string
     */
    private String createFileFooter() {
        StringBuilder footer = new StringBuilder();
        footer.append("</trkseg>\n</trk>\n<time>")
                .append(sdf.format(System.currentTimeMillis()))
                .append("</time>\n</gpx>");

        return footer.toString();
    }
}