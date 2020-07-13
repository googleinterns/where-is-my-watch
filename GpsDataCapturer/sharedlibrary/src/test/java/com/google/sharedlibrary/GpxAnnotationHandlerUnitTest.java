package com.google.sharedlibrary;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Environment;

import com.google.sharedlibrary.gpxfile.GpxAnnotationHandler;
import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.gpxfile.GpxWriteHandler;
import com.google.sharedlibrary.storage.GpxFileFolder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpxAnnotationHandlerUnitTest {
    private GpxAnnotationHandler gpxAnnotationHandler;

    @Mock
    private Context context;
    @Mock
    private File gpxFile;

    private boolean append = true;
    private boolean isHeader = true;


    @Before
    public void setUp(){
        ShadowLog.stream = System.out;
        context = mock(Context.class);
        gpxFile = mock(File.class);
        gpxAnnotationHandler = new GpxAnnotationHandler(context, gpxFile, append, isHeader);
    }

    @Test
    public void testRun() throws Exception {
        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        executor.execute(gpxAnnotationHandler);
        verify(executor).execute(gpxAnnotationHandler);
    }

    @After
    public void tearDown(){
        context = null;
        gpxFile = null;
        gpxAnnotationHandler = null;
    }

}
