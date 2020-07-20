package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.location.Location;
import android.os.Build;

import com.google.sharedlibrary.model.GpsData;
import com.google.sharedlibrary.model.GpsInfoViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpsInfoViewModelUnitTest {

    private GpsInfoViewModel gpsInfoViewModel;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        gpsInfoViewModel = new GpsInfoViewModel();
    }

    @Test
    public void testSetGpsStatusMutableLiveData() {
        gpsInfoViewModel.setGpsStatusMutableLiveData(1);
        assertNotNull(gpsInfoViewModel.getGpsStatusMutableLiveData().getValue());
        assertEquals("Gps Status: GPS_EVENT_STARTED",
                gpsInfoViewModel.getGpsStatusMutableLiveData().getValue());

        gpsInfoViewModel.setGpsStatusMutableLiveData(2);
        assertEquals("Gps Status: GPS_EVENT_STOPPED",
                gpsInfoViewModel.getGpsStatusMutableLiveData().getValue());

        gpsInfoViewModel.setGpsStatusMutableLiveData(3);
        assertEquals("Gps Status: GPS_EVENT_FIRST_FIX",
                gpsInfoViewModel.getGpsStatusMutableLiveData().getValue());
    }

    @Test
    public void testSetGpsDataMutableLiveData() {
        Location location = mock(Location.class);
        gpsInfoViewModel.setGpsDataMutableLiveData(location);

        GpsData gpsData = new GpsData(location);

        assertNotNull(gpsInfoViewModel.getGpsDataMutableLiveData().getValue());
        assertEquals(gpsData.getGpsDataString(),
                gpsInfoViewModel.getGpsDataMutableLiveData().getValue().getGpsDataString());
    }

    @After
    public void tearDown() {
        gpsInfoViewModel = null;
    }
}
