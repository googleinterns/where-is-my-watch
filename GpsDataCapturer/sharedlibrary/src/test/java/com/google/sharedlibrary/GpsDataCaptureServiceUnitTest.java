package com.google.sharedlibrary;

import static android.content.Context.CONNECTIVITY_SERVICE;

import static com.google.sharedlibrary.gpxfile.GpxFileHelper.createGpxFile;
import static com.google.sharedlibrary.gpxfile.GpxFileHelper.getNewFileName;
import static com.google.sharedlibrary.storage.GpxFileFolder.createGpsDataFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.service.GpsDataCaptureService.GpsDataCaptureBinder;
import com.google.sharedlibrary.utils.Utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowContextWrapper;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowService;

import java.io.File;
import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpsDataCaptureServiceUnitTest {
    private GpsDataCaptureService gpsDataCaptureService;
    @Mock
    private LocationManager locationManager;

    @Mock
    private LocationManagerListener locationManagerListener;


    @Mock
    private File gpxFile;

    private File gpxFileFolder;
    private GpxFileWriter gpxFileWriter;

    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        gpsDataCaptureService = Robolectric.buildService(
                GpsDataCaptureService.class).create().get();

        locationManager =
                (LocationManager) gpsDataCaptureService.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = Shadows.shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);

        gpxFileFolder = createGpsDataFolder(gpsDataCaptureService);

        locationManagerListener = mock(LocationManagerListener.class);
    }

    @Test
    public void testServiceStartCapture() {
        ShadowLog.stream = System.out;
        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);

        assertEquals(1, gpxFileFolder.listFiles().length);
        assertEquals(1, shadowLocationManager.getRequestLocationUpdateListeners().size());

        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);
        assertEquals(2, gpxFileFolder.listFiles().length);
    }

    @Test
    public void testOnLocationChanged() throws Exception {
        //Given
        GpsInfoViewModel gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsDataCaptureService.setGpsInfoViewModel(gpsInfoViewModel);

        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);

        Location location = mock(Location.class);
        shadowLocationManager.simulateLocation(location);

        //When
        gpsDataCaptureService.onLocationChanged(location);


        //Then
        verify(gpsInfoViewModel).setGpsDataMutableLiveData(location);
        verify(gpsInfoViewModel, times(1)).setGpsDataMutableLiveData(location);

        //When
        gpsDataCaptureService.onLocationChanged(location);
        verify(gpsInfoViewModel, times(2)).setGpsDataMutableLiveData(location);


    }

    @Test
    public void testOnGpsStatusChanged() {
        //Given
        GpsInfoViewModel gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsDataCaptureService.setGpsInfoViewModel(gpsInfoViewModel);
        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);

        //When
        gpsDataCaptureService.onGpsStatusChanged(1);

        //Then
        verify(gpsInfoViewModel).setGpsStatusMutableLiveData(1);
        verify(gpsInfoViewModel, times(1)).setGpsStatusMutableLiveData(1);

        //When
        gpsDataCaptureService.onGpsStatusChanged(1);

        //Then
        verify(gpsInfoViewModel, times(2)).setGpsStatusMutableLiveData(1);

    }

    @Test
    public void testServiceStopCapture() {
        ShadowLog.stream = System.out;
        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);
        gpsDataCaptureService.stopCapture(Utils.LocationApiType.LOCATIONMANAGER);

        assertEquals(1, gpxFileFolder.listFiles().length);
        assertEquals(0, shadowLocationManager.getRequestLocationUpdateListeners().size());
    }
}
