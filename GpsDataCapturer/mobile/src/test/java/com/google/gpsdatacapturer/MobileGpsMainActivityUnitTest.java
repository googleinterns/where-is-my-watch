package com.google.gpsdatacapturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class MobileGpsMainActivityUnitTest {

    private MobileGpsMainActivity mobileGpsMainActivity;
    private Button startAndStopButton;
    private RadioGroup apiRadioGroup;
    private RadioButton lmRadioButton;
    private RadioButton flpRadioButton;
    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;

    @Before
    public void setUp() {
        mobileGpsMainActivity = Robolectric.buildActivity(MobileGpsMainActivity.class).create().get();
        startAndStopButton = mobileGpsMainActivity.findViewById(R.id.m_start_stop_button);
        apiRadioGroup = mobileGpsMainActivity.findViewById(R.id.m_raido_group_api);
        lmRadioButton = mobileGpsMainActivity.findViewById(R.id.m_radio_button_LM);
        flpRadioButton = mobileGpsMainActivity.findViewById(R.id.m_radio_button_FPL);
        gpsDataTextView = mobileGpsMainActivity.findViewById(R.id.m_text_view_gps_data);
        gpsStatusTextView = mobileGpsMainActivity.findViewById(R.id.m_text_view_gps_status);

    }

    @Test
    public void testWearGpsMainActivityNotNull() {
        assertNotNull(mobileGpsMainActivity);
    }

    @Test
    public void testButtonsAndViewsNotNull() {
        assertNotNull(startAndStopButton);
        assertNotNull(apiRadioGroup);
        assertNotNull(lmRadioButton);
        assertNotNull(flpRadioButton);
        assertNotNull(gpsDataTextView);
        assertNotNull(gpsStatusTextView);
    }

    @Test
    public void testInitialUIShowingCorrect() {
        assertEquals(View.VISIBLE, apiRadioGroup.getVisibility());
        assertEquals(View.VISIBLE, startAndStopButton.getVisibility());
        assertEquals("START", startAndStopButton.getText());
        assertEquals(View.GONE, gpsDataTextView.getVisibility());
        assertEquals(View.GONE, gpsStatusTextView.getVisibility());
    }

    @Test
    public void testLmRadioButtonChecked() {
        lmRadioButton.performClick();
        assertEquals(R.id.m_radio_button_LM, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testFlpRadioButtonChecked() {
        flpRadioButton.performClick();
        assertEquals(R.id.m_radio_button_FPL, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testApiRadioGroupHideOnStartButtonClicked() {
        startAndStopButton.performClick();
        assertEquals(View.GONE, apiRadioGroup.getVisibility());
    }

    @Test
    public void testStartAndStopButtonShowCorrectText() {
        assertEquals("START", startAndStopButton.getText());
        startAndStopButton.performClick();
        assertEquals("STOP", startAndStopButton.getText());
    }

    @Test
    public void testStartButtonClickShouldStartNewIntentService() {
        lmRadioButton.performClick();
        startAndStopButton.performClick();

        Intent expectedIntent = new Intent(mobileGpsMainActivity, GpsDataCaptureService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(mobileGpsMainActivity);
        Intent shadowIntent = shadowActivity.getNextStartedService();

        assertTrue(shadowIntent.filterEquals(expectedIntent));
        assertEquals(View.VISIBLE, gpsDataTextView.getVisibility());
        assertEquals(View.VISIBLE, gpsStatusTextView.getVisibility());
    }
}
