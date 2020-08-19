package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import com.google.sharedlibrary.model.SatelliteSignalData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.PriorityQueue;

@RunWith(JUnit4.class)
public class SatellitesSignalDataUnitTest {
    private SatelliteSignalData signalData;
    private PriorityQueue<Float> pq;

    @Before
    public void setUp(){
        signalData = new SatelliteSignalData();
        pq = new PriorityQueue<>();
    }

    @Test
    public void testSetSignalDataSuccess(){
        Float[] signals = new Float[] {20.375f, 36.156f, 33.945f, 29.188f};
        float average = (20.375f + 36.156f + 33.945f + 29.188f) / 4;
        pq.addAll(Arrays.asList(signals));

        signalData.setSignalData(pq);
        assertEquals(36.156f, signalData.getFirstSignal(), 0.0f);
        assertEquals(33.945f, signalData.getSecondSignal(), 0.0f);
        assertEquals(29.188f, signalData.getThirdSignal(), 0.0f);
        assertEquals(20.375f, signalData.getForthSignal(), 0.0f);
        assertEquals(average, signalData.getAverageSignal(), 0.0f);
    }

    @Test
    public void testSetSignalDataFailure(){
        Float[] signals = new Float[] {20.375f, 36.156f, 33.945f};
        pq.addAll(Arrays.asList(signals));

        signalData.setSignalData(pq);
        assertEquals(0.0f, signalData.getFirstSignal(), 0.0f);
        assertEquals(0.0f, signalData.getSecondSignal(), 0.0f);
        assertEquals(0.0f, signalData.getThirdSignal(), 0.0f);
        assertEquals(0.0f, signalData.getForthSignal(), 0.0f);
        assertEquals(0.0f, signalData.getAverageSignal(), 0.0f);
    }

    @After
    public void tearDown(){
        signalData = null;
        pq = null;
    }
}
