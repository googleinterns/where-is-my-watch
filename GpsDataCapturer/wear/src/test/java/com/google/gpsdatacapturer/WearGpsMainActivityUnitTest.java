package com.google.gpsdatacapturer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.Build;
import android.widget.Button;

import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

@RunWith (RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class WearGpsMainActivityUnitTest {
    private WearGpsMainActivity wearGpsMainActivity;
    private Button startAndStopButton;

    @Before
    public void setup(){
        wearGpsMainActivity = Robolectric.buildActivity(WearGpsMainActivity.class).create().get();
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
    }

    @Test
    public void testWearGpsMainActivityNotNull(){
        assertNotNull(wearGpsMainActivity);
    }

    @Test
    public void testRadioGroupNotNull(){
        assertNotNull(wearGpsMainActivity.findViewById(R.id.radio_group_location_api));
        assertNotNull(wearGpsMainActivity.findViewById(R.id.radio_button_LM));
        assertNotNull(wearGpsMainActivity.findViewById(R.id.radio_button_FPL));
    }

    //Test GpsDataCaptureService is on using LocationManager API
    @Test
    public void testGpsDataCaptureServiceIsOnUsingLocatioManager(){
        wearGpsMainActivity.findViewById(R.id.radio_button_LM).performClick();
        startAndStopButton.performClick();

        Intent expectedIntent = new Intent(wearGpsMainActivity, GpsDataCaptureService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(wearGpsMainActivity);
        Intent shadowIntent = shadowActivity.getNextStartedService();

        assertEquals(true, shadowIntent.filterEquals(expectedIntent));
    }

    //Test GpsDataTextView is updating data
    @Test
    public void testGpsDataTextViewIsUpdating(){

    }

    //Test GpsStatusTextView is updating data
    @Test
    public void testGpsStatusTextViewIsUpdating(){

    }

    //Test GpsDataCaptureService is off using LocationManager API
    @Test
    public void testGpsDataCaptureServiceIsOffUsingLocatioManager(){
        startAndStopButton.performClick();

    }

    //Test  GpsDataCaptureService is on using FusedLocationProvider API
    @Test
    public void testGpsDataCaptureServiceIsOnUsingFLP(){

    }

    //Test  GpsDataCaptureService is off using FusedLocationProvider API
    @Test
    public void testGpsDataCaptureServiceIsOffUsingFLP(){

    }
}
