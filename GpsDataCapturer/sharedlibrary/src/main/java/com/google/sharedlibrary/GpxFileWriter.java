package com.google.sharedlibrary;

import android.content.Context;
import android.location.Location;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class provides write function helping GpxFile writing captured gps data to the file.
 */
public class GpxFileWriter {
    private final static String TAG = "GpxFileWriter";
    private static ThreadPoolExecutor EXECUTOR;
    protected File gpxFile;
    private boolean append;

    public GpxFileWriter(File gpxFile, boolean append) {
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

    public void write(Context context, Location location, boolean isNewFile) throws Exception {
        long time = location.getTime();
        if (time <= 0) {
            time = System.currentTimeMillis();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                context.getResources().getConfiguration().locale);

        Runnable writeHandler = new GpxWriteHandler(context, sdf.format(time), gpxFile, location,
                append, isNewFile);
        EXECUTOR.execute(writeHandler);
    }
}
