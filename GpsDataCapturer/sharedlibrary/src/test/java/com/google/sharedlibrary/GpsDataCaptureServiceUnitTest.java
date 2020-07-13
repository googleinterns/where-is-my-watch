package com.google.sharedlibrary;

import static com.google.sharedlibrary.storage.GpxFileFolder.createGpsDataFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import com.google.sharedlibrary.gpxfile.GpxFileWriter;
import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.service.GpsDataCaptureService;
import com.google.sharedlibrary.utils.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
    public void testServiceStartCapture() throws IOException {
        ShadowLog.stream = System.out;
        gpsDataCaptureService.startCapture(Utils.LocationApiType.LOCATIONMANAGER);

        assertNotNull(gpxFileFolder.listFiles());
        assertEquals(1, gpxFileFolder.listFiles().length);
        assertEquals(1, shadowLocationManager.getRequestLocationUpdateListeners().size());

        for(File file: gpxFileFolder.listFiles()){
            assertNotNull(file);
            assertTrue(file.exists());
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder content = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine()) != null){
                    content.append(line);
                    content.append(System.lineSeparator());
                }
                System.out.println(content);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

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

        assertNotNull(gpxFileFolder.listFiles());
        assertEquals(1, gpxFileFolder.listFiles().length);
        assertEquals(0, shadowLocationManager.getRequestLocationUpdateListeners().size());
    }

    @After
    public void tearDown(){
        gpsDataCaptureService.onDestroy();
    }
}
