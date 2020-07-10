package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.sharedlibrary.model.GpsData;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class GpsDataCaptureServiceUnitTest {
    @Rule
    public MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Mock
    private Context context;

    private GpsDataCaptureService gpsDataCaptureService;

    @Mock
    private GpsInfoViewModel gpsInfoViewModel;

    @Mock
    private LiveData<GpsData> gpsDataLiveData;

    @Mock
    private LiveData<String> gpsStatusLiveData;

    @Mock
    private Observer<GpsData> gpsDataObserver;

    @Mock
    private Observer<String> gpsStatusObserver;

    @Before
    public void setUp(){
        gpsDataCaptureService = Robolectric.buildService(GpsDataCaptureService.class).create().get();
        MockitoAnnotations.initMocks(this);

        gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsDataLiveData = new MutableLiveData<>();
        gpsStatusLiveData = new MutableLiveData<>();
        when(gpsInfoViewModel.getGpsDataMutableLiveData()).thenReturn(gpsDataLiveData);
        when(gpsInfoViewModel.getGpsStatusMutableLiveData()).thenReturn(gpsStatusLiveData);
    }

    @Test
    public void testGpsDataLiveDataEmitting(){
        Location location = createLocation(37.31032348, -122.03040386, 2.0f);
        GpsInfoViewModel.setGpsDataMutableLiveData(location);
        gpsInfoViewModel.getGpsDataMutableLiveData().observeForever(
                gpsDataObserver);
        gpsInfoViewModel.getGpsStatusMutableLiveData().observeForever(gpsStatusObserver);
        assertEquals(gpsInfoViewModel.getGpsDataMutableLiveData().getValue(), location);
    }


    //Test View MODEL

    //Test File Writer


    private Location createLocation(double lat, double lon, float speed) {
        //Create a new Location
        Location location = new Location("GPS");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setSpeed(speed);
        return location;
    }
}
