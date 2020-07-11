package com.google.sharedlibrary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.location.Location;
import android.os.Build;

import com.google.sharedlibrary.gpxfile.GpxAnnotationHandler;
import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.gpxfile.GpxWriteHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(MockitoJUnitRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpxFileWriteHandlerUnitTest {
    private GpxFileWriter gpxFileWriter;
    private Context context;
    private File gpxFile;
    private boolean append;
    private ThreadPoolExecutor EXECUTOR;

    @Before
    public void setUp(){
        context = mock(Context.class);
        gpxFile = mock(File.class);
        gpxFileWriter = new GpxFileWriter(context, gpxFile, append);
    }

    @Test
    public void testGpxWriteHandlerRun() throws Exception {
        Location location = mock(Location.class);
        location.setLatitude(37.2345);
        location.setLongitude(-122.44433);
        location.setSpeed(1.0f);

//        when(context.getResources()).thenReturn()
        SimpleDateFormat sdf = mock(SimpleDateFormat.class);
        Runnable writeHandler = mock(GpxWriteHandler.class);
        gpxFileWriter.writeGpsData(null);
        writeHandler.run();
    }
}
