package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.sharedlibrary.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class GpxFooterHandler implements Runnable {
    private static final String TAG = "GpxFooterHandler";
    private final File gpxFile;
    private final Context context;
    private final boolean append;

    public GpxFooterHandler(Context context, File gpxFile, boolean append) {
        this.context = context;
        this.gpxFile = gpxFile;
        this.append = append;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        Log.d(TAG, "Writing the xml footer.");
        try (FileWriter fileWriter = new FileWriter(gpxFile, true)) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(createFileFooter());
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (Exception e) {
            Log.e(TAG, "GpxFileWriter.write", e);
        }
    }

    private String createFileFooter(){
        StringBuilder footer = new StringBuilder();
        footer.append("</trkseg>\n</trk>\n<time>")
                .append(Utils.getFormattedCurrentTime(context))
                .append("</time>\n</gpx>");

        return footer.toString();
    }
}