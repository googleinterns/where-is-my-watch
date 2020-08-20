package com.google.sharedlibrary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.location.Location;
import android.os.Build;

import com.google.sharedlibrary.gpxfile.GpxWriteHandler;
import com.google.sharedlibrary.model.SatelliteSignalData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpxWriteHandlerUnitTest {
    private GpxWriteHandler gpxWriteHandler;

    @Mock
    private File gpxFile;
    @Mock
    private Location location;


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        gpxFile = mock(File.class);
        location = mock(Location.class);
        SatelliteSignalData signalData = new SatelliteSignalData();
        String formattedTime = "2020-07-12T00:02:36.000Z";
        boolean append = true;
        gpxWriteHandler = new GpxWriteHandler(formattedTime, gpxFile, location, signalData, append);
    }

    @Test
    public void testRun() throws Exception {
        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        executor.execute(gpxWriteHandler);
        verify(executor).execute(gpxWriteHandler);
    }

    @After
    public void tearDown() {
        gpxFile = null;
        location = null;
        gpxWriteHandler = null;
    }
}
