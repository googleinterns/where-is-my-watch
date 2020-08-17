package com.google.sharedlibrary.gpxfile;

import android.content.Context;
import android.location.Location;

import com.google.sharedlibrary.model.SatelliteSignalData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class provides write function helping GpxFile writing captured gps data to the file.
 */
public class GpxFileWriter {
    private final static String TAG = "GpxFileWriter";
    private SimpleDateFormat sdf;
    protected File gpxFile;
    private boolean append;
    protected ThreadPoolExecutor EXECUTOR;

    public GpxFileWriter(SimpleDateFormat sdf, File gpxFile, boolean append) {
        this.sdf = sdf;
        this.gpxFile = gpxFile;
        this.append = append;
        EXECUTOR = new ThreadPoolExecutor(1, 1, 60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(10), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            }
        });
    }

    /**
     * Write the gpx data into file with header if it's new file
     *
     * @param location the updated location
     * @throws Exception
     */
    public void writeGpsData(Location location, SatelliteSignalData signalData) throws Exception {
        long time = location.getTime();
        if (time <= 0) {
            time = System.currentTimeMillis();
        }

        Runnable writeHandler = new GpxWriteHandler(sdf.format(time), gpxFile, location, signalData,
                append);
        EXECUTOR.execute(writeHandler);
    }

    /**
     * Write the gpx file footer in xml format
     */
    public void writeFileAnnotation(boolean isHeader) {
        Runnable gpxAnnotationHandler = new GpxAnnotationHandler(sdf, gpxFile, append, isHeader);
        EXECUTOR.execute(gpxAnnotationHandler);
    }
}
