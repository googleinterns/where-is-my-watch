package com.google.sharedlibrary;

import static android.content.Context.CONNECTIVITY_SERVICE;

import static com.google.sharedlibrary.gpxfile.GpxFileHelper.createGpxFile;
import static com.google.sharedlibrary.gpxfile.GpxFileHelper.getNewFileName;
import static com.google.sharedlibrary.storage.GpxFileFolder.createGpsDataFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.service.GpsDataCaptureService.GpsDataCaptureBinder;
import com.google.sharedlibrary.utils.Utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLocationManager;

import java.io.File;
import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpsDataCaptureServiceUnitTest {
    private GpsDataCaptureService gpsDataCaptureService;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Mock
    private Context context;

    @Mock
    private Resources resoucrces;

    @Mock
    private Configuration mConfiguration;

    @Mock
    private File gpxFile;

    @Mock
    private File gpxFileFolder;

    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() {
        gpsDataCaptureService = Robolectric.buildService(GpsDataCaptureService.class).create().get();
        locationManager = mock(LocationManager.class);
        locationListener = mock(LocationListener.class);
        context = mock(Context.class);
        resoucrces = mock(Resources.class);
        mConfiguration = mock(Configuration.class);
        when(context.getResources()).thenReturn(resoucrces);
        when(resoucrces.getConfiguration()).thenReturn(mConfiguration);
        when(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(CONNECTIVITY_SERVICE);

        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void testServiceStartCapture() {
//        gpxFileFolder = mock(File.class);
//        when(gpxFileFolder.getPath()).thenReturn("/Users/lynnzl/Downloads");
//        gpsDataCaptureService.startService(new Intent(context, GpsDataCaptureService.class));
//        gpxFile = mock(File.class);


        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);


//        shadowLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
//                0, locationListener);
//        verify(locationManager, times(1)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
//                0, locationListener);
        Location location = mock(Location.class);
        locationListener.onLocationChanged(location);

        shadowLocationManager.simulateLocation(location);
        verify(locationManager, times(1)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                0, locationListener);


    }

    @Test
    public void testSetGpsInfoViewModel(){

    }

    @Test
    public void testOnLocationChanged(){

    }

    @Test
    public void testOnGpsStatusChanged(){

    }
    @Test
    public void testServiceStopCapture() {

    }



    //Test View MODEL

    //Test File Writer

}
