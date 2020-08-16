package com.google.sharedlibrary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.location.Location;
import android.os.Build;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.sharedlibrary.locationhelper.FusedLocationProviderListener;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class FusedLocationProviderListenerUnitTest {
    private FusedLocationProviderListener flpListener;

    @Mock
    private FusedLocationProviderClient flpClient;

    @Mock
    private GpsDataCaptureService service;

    @Mock
    private Location location;

    @Before
    public void setUp(){
        ShadowLog.stream = System.out;
        flpClient = mock(FusedLocationProviderClient.class);
        service = mock(GpsDataCaptureService.class);
        flpListener = new FusedLocationProviderListener(service);
    }

    @Test
    public void testonLocationResult(){
        //Given
        location = mock(Location.class);
        List<Location> locationList = new ArrayList<>();
        locationList.add(location);
        LocationResult locationResult = LocationResult.create(locationList);

        //When
        flpListener.onLocationResult(locationResult);

        //Then
        verify(service).onLocationChanged(location);
        verify(service, times(1)).onLocationChanged(location);

        //When
        flpListener.onLocationResult(locationResult);
        //Then
        verify(service, times(2 )).onLocationChanged(location);
    }

    @After
    public void tearDown(){
        service.onDestroy();
        flpListener = null;
    }
}
