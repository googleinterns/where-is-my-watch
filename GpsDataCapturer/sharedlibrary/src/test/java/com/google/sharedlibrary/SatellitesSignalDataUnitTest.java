package com.google.sharedlibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void setUp() {
        pq = new PriorityQueue<>();
    }

    @Test
    public void testSatelliteSignalDataSuccessWithPQ() {
        Float[] signals = new Float[]{20.375f, 36.156f, 33.945f, 29.188f};
        float average = (20.375f + 36.156f + 33.945f + 29.188f) / 4;
        pq.addAll(Arrays.asList(signals));

        signalData = new SatelliteSignalData(pq);
        assertTrue(pq.isEmpty());
        assertEquals(36.156f, signalData.getFirstSignal(), 0.0f);
        assertEquals(33.945f, signalData.getSecondSignal(), 0.0f);
        assertEquals(29.188f, signalData.getThirdSignal(), 0.0f);
        assertEquals(20.375f, signalData.getForthSignal(), 0.0f);
        assertEquals(average, signalData.getAverageSignal(), 0.0f);
    }

    @Test
    public void testSatelliteSignalDataSuccess() {
        signalData = new SatelliteSignalData();
        assertEquals(0.0f, signalData.getFirstSignal(), 0.0f);
        assertEquals(0.0f, signalData.getSecondSignal(), 0.0f);
        assertEquals(0.0f, signalData.getThirdSignal(), 0.0f);
        assertEquals(0.0f, signalData.getForthSignal(), 0.0f);
        assertEquals(0.0f, signalData.getAverageSignal(), 0.0f);
    }

    @Test
    public void testSatelliteSignalDataFailure() {
        Float[] signals = new Float[]{20.375f, 36.156f, 33.945f};
        pq.addAll(Arrays.asList(signals));

        try {
            signalData = new SatelliteSignalData(pq);
            assertEquals(0.0f, signalData.getFirstSignal(), 0.0f);
            assertEquals(0.0f, signalData.getSecondSignal(), 0.0f);
            assertEquals(0.0f, signalData.getThirdSignal(), 0.0f);
            assertEquals(0.0f, signalData.getForthSignal(), 0.0f);
            assertEquals(0.0f, signalData.getAverageSignal(), 0.0f);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSatelliteSignalDataEqual() {
        Float[] signals = new Float[]{20.375f, 36.156f, 33.945f, 29.188f};
        pq.addAll(Arrays.asList(signals));
        SatelliteSignalData signalData1 = new SatelliteSignalData(pq);

        pq.addAll(Arrays.asList(signals));
        SatelliteSignalData signalData2 = new SatelliteSignalData(pq);

        signals = new Float[]{20.375f, 36.156f, 33.945f, 35.123f};
        pq.addAll(Arrays.asList(signals));
        SatelliteSignalData signalData3 = new SatelliteSignalData(pq);

        assertTrue(signalData1.equals(signalData2));
        assertFalse(signalData1.equals(signalData3));
        assertFalse(signalData2.equals(signalData3));
    }

    @Test
    public void testSatelliteSignalDataHashcode() {
        Float[] signals = new Float[]{20.375f, 36.156f, 33.945f, 29.188f};
        pq.addAll(Arrays.asList(signals));
        SatelliteSignalData signalData1 = new SatelliteSignalData(pq);

        signals = new Float[]{20.375f, 36.156f, 33.945f, 35.123f};
        pq.addAll(Arrays.asList(signals));
        SatelliteSignalData signalData2 = new SatelliteSignalData(pq);

        assertNotEquals(signalData1.hashCode(), signalData2.hashCode());
    }

    @After
    public void tearDown() {
        signalData = null;
        pq = null;
    }
}
