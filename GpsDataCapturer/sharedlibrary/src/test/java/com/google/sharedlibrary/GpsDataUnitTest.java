package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.location.Location;

import com.google.sharedlibrary.model.GpsData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GpsDataUnitTest {
    private GpsData gpsData;

    @Test
    public void testGetGpsDataString() {
        //Given
        String expected =
                "GPS DATA \n" + "Lat: " + "0.0\n" + "Lon: " + "0.0\n" + "Speed: " + "0.0\n";
        Location location = mock(Location.class);

        //When
        gpsData = new GpsData(location);

        //Then
        assertEquals(expected, gpsData.getGpsDataString());
    }
}
