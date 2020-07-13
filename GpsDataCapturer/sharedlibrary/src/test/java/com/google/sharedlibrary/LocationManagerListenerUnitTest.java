package com.google.sharedlibrary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;

import com.google.sharedlibrary.locationhelper.LocationManagerListener;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class LocationManagerListenerUnitTest {
    private LocationManagerListener lmListener;

    @Mock
    private GpsDataCaptureService service;

    @Mock
    private Location location;

    @Before
    public void setUp(){
        service = mock(GpsDataCaptureService.class);
        lmListener = new LocationManagerListener(service);
    }

    @Test
    public void testOnLocationChanged(){
        //Given
        location = mock(Location.class);

        //When
        lmListener.onLocationChanged(location);

        //Then
        verify(service).onLocationChanged(location);
        verify(service, times(1)).onLocationChanged(location);

        //When
        lmListener.onLocationChanged(location);

        //Then
        verify(service,times(2)).onLocationChanged(location);
    }

    @Test
    public void testOnGpsStatusChanged(){
        lmListener.onGpsStatusChanged(1);
        verify(service).onGpsStatusChanged(1);
        verify(service, times(1)).onGpsStatusChanged(1);

        lmListener.onGpsStatusChanged(1);
        verify(service, times(2)).onGpsStatusChanged((1));
    }

    @After
    public void tearDown(){
        service.onDestroy();
        lmListener = null;
    }

}
