package com.google.sharedlibrary;

import android.content.Context;
import android.location.Location;
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
    private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(10), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //
        }
    });

    private static File gpxFile;

    public GpxFileWriter(File gpxFile){
        this.gpxFile = gpxFile;
    }

    public static void write(Context context, Location location) throws Exception{
        long time = location.getTime();
        if(time <= 0){
            time = System.currentTimeMillis();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("PST"));
        String formattedTime = sdf.format(time);

        Runnable writeHandler = new GpxWriteHandler(formattedTime, gpxFile, location);
        EXECUTOR.execute(writeHandler);
    }

    /**
     * Create the xml header with version, creator and metadata
     * @param formattedTime time of on location changed in format
     * @return header string
     */
    public static String xmlHeader(String formattedTime){
        StringBuilder header = new StringBuilder();
        header.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        header.append("<gpx version=\"1.1\" creator=\"GpsDataCapturer " + BuildConfig.VERSION_CODE + "\" ");
        header.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        header.append("xmlns=\"http://www.topografix.com/GPX/1/1\" ");
        header.append("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 ");
        header.append("http://www.topografix.com/GPX/1/1/gpx.xsd\">");
        header.append("<metadata><time>").append(formattedTime).append("</time></metadata>");
        header.append("<trk>");
        return header.toString();
    }
}
