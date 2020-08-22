package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.location.Location;

import com.google.sharedlibrary.model.GpsData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.DecimalFormat;

@RunWith(MockitoJUnitRunner.class)
public class GpsDataUnitTest {
    private GpsData gpsData;
    private DecimalFormat locationDF;
    private DecimalFormat speedDF;

    @Test
    public void testGetGpsDataString() {
        //Given
        Location location = mock(Location.class);
        locationDF= new DecimalFormat("0.000000");
        speedDF = new DecimalFormat("0.0000");

        //When
        gpsData = new GpsData(location);

        //Then
        assertEquals(locationDF.format(location.getLatitude()), gpsData.getLatitude());
        assertEquals(locationDF.format(location.getLongitude()), gpsData.getLongitude());
        assertEquals(speedDF.format(location.getSpeed()), gpsData.getSpeed());
    }
}
