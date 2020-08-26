package com.google.sharedlibrary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Build;

import com.google.sharedlibrary.gpxfile.GpxAnnotationHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpxAnnotationHandlerUnitTest {
  private GpxAnnotationHandler gpxAnnotationHandler;

  @Mock private File gpxFile;

  private boolean append = true;
  private boolean isHeader = true;

  @Before
  public void setUp() {
    ShadowLog.stream = System.out;
    Locale locale = new Locale("en", "US");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", locale);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    gpxFile = mock(File.class);
    gpxAnnotationHandler = new GpxAnnotationHandler(sdf, gpxFile, append, isHeader);
  }

  @Test
  public void testRun() throws Exception {
    ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
    executor.execute(gpxAnnotationHandler);
    verify(executor).execute(gpxAnnotationHandler);
  }

  @After
  public void tearDown() {
    gpxFile = null;
    gpxAnnotationHandler = null;
  }
}
