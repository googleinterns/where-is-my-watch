package com.google.sharedlibrary;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GpxFileWriter {
    private final static String TAG = "GpxFileWriter";
    protected final static Object lock = new Object();
    private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(10), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        }
    });

    protected File gpxFile;
    private boolean append;

    public GpxFileWriter(File gpxFile, boolean append) {
        this.gpxFile = gpxFile;
        this.append = append;
    }

    public void write(Context context, Location location) throws Exception {
        long time = location.getTime();
        if (time <= 0) {
            time = System.currentTimeMillis();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        Runnable writeHandler = new GpxWriteHandler(sdf.format(time), gpxFile, location, append);
        EXECUTOR.execute(writeHandler);
    }
}
