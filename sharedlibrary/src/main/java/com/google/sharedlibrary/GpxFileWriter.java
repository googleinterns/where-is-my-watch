package com.google.sharedlibrary;

import android.location.Location;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GpxFileWriter {
    protected final static Object lock = new Object();
    private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(10), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //
        }
    });

    private static File gpxFile = null;
    private static boolean addNewSegment;

    public GpxFileWriter(File gpxFile, boolean addNewSegment){
        this.gpxFile = gpxFile;
        this.addNewSegment = addNewSegment;
    }

    public static void write(Location location) throws Exception{
        long time = location.getTime();
        if(time <= 0){
            time = System.currentTimeMillis();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedTime = sdf.format(time);

        Runnable writeHandler = new GpxWriteHandler(formattedTime, gpxFile, location, addNewSegment);
        EXECUTOR.execute(writeHandler);
    }
}
