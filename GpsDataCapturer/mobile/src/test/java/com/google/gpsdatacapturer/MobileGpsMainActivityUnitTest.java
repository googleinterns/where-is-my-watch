package com.google.gpsdatacapturer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gpsdatacapturer.databinding.ActivityMobileGpsMainBinding;
import com.google.sharedlibrary.model.GpsData;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowService;
import org.robolectric.shadows.ShadowSettings;

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
    public void setUp() {
        mobileGpsMainActivity = Robolectric.buildActivity(MobileGpsMainActivity.class).create().get();
        startAndStopButton = mobileGpsMainActivity.findViewById(R.id.m_start_stop_button);
        apiRadioGroup = mobileGpsMainActivity.findViewById(R.id.m_raido_group_api);
        lmRadioButton = mobileGpsMainActivity.findViewById(R.id.m_radio_button_LM);
        flpRadioButton = mobileGpsMainActivity.findViewById(R.id.m_radio_button_FLP);
        gpsDataTextView = mobileGpsMainActivity.findViewById(R.id.m_text_view_gps_data);
        gpsStatusTextView = mobileGpsMainActivity.findViewById(R.id.m_text_view_gps_status);
    }

    @Test
    public void testWearGpsMainActivityNotNull() {
        assertNotNull(mobileGpsMainActivity);
    }

    //The following test testing UI behaves as expected
    @Test
    public void testUIComponentsNotNull() {
        assertNotNull(startAndStopButton);
        assertNotNull(apiRadioGroup);
        assertNotNull(lmRadioButton);
        assertNotNull(flpRadioButton);
        assertNotNull(gpsDataTextView);
        assertNotNull(gpsStatusTextView);

        View layout = LayoutInflater.from(mobileGpsMainActivity).inflate(
                R.layout.activity_mobile_gps_main,
                null);
        assertNotNull(layout);
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
        assertEquals(R.id.m_radio_button_FLP, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testApiRadioGroupHideOnStartButtonClicked() {
        startAndStopButton.performClick();
        assertEquals(View.GONE, apiRadioGroup.getVisibility());
    }

    @Test
    public void testTextViewsVisibleOnStartButtonClicked() {
        startAndStopButton.performClick();
        assertEquals(View.VISIBLE, gpsDataTextView.getVisibility());
        assertEquals(View.VISIBLE, gpsStatusTextView.getVisibility());
    }

    @Test
    public void testStartAndStopButtonShowCorrectText() {
        assertEquals("START", startAndStopButton.getText());
        startAndStopButton.performClick();
        assertEquals("STOP", startAndStopButton.getText());
    }

    //The following test testing data binding successfully
    @Test
    public void testDataBindingSuccess() {
        ActivityMobileGpsMainBinding binding =
                DataBindingUtil.setContentView(mobileGpsMainActivity,
                        R.layout.activity_mobile_gps_main);
        binding.setGpsInfoViewModel(gpsInfoViewModel);
        binding.setLifecycleOwner(mobileGpsMainActivity);
        assertNotNull(binding);
        assertEquals(binding.getGpsInfoViewModel(), gpsInfoViewModel);
        assertEquals(binding.getLifecycleOwner(), mobileGpsMainActivity);
    }

    //The following test testing gpsDataCapture service start and stop as expected
    @Test
    public void testStartButtonClickShouldStartService() {
        lmRadioButton.performClick();
        startAndStopButton.performClick();

        Intent expectedIntent = new Intent(mobileGpsMainActivity, GpsDataCaptureService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(mobileGpsMainActivity);
        Intent shadowIntent = shadowActivity.getNextStartedService();

        assertTrue(shadowIntent.filterEquals(expectedIntent));
    }

    @Test
    public void testStopButtonClickShouldStopService() {
        ServiceConnection coon = mock(ServiceConnection.class);
        GpsDataCaptureService service = Robolectric.buildService(GpsDataCaptureService.class).create().get();
        service.bindService(new Intent(mobileGpsMainActivity, GpsDataCaptureService.class), coon, Context.BIND_AUTO_CREATE);

        startAndStopButton.performClick();
        service.unbindService(coon);
        service.stopSelf();

        ShadowService shadowService = Shadows.shadowOf(service);

        //THEN, check if service stops itself
        assertTrue(shadowService.isStoppedBySelf());
    }

    //The following tests testing gpsInfoViewModel behave as expected
    @Test
    public void testGpsInfoViewModelNotNull() {
        gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsDataLiveData = new MutableLiveData<>();
        gpsStatusLiveData = new MutableLiveData<>();
        when(gpsInfoViewModel.getGpsDataMutableLiveData()).thenReturn(gpsDataLiveData);
        when(gpsInfoViewModel.getGpsStatusMutableLiveData()).thenReturn(gpsStatusLiveData);
        gpsInfoViewModel.getGpsDataMutableLiveData().observeForever(
                gpsDataObserver);
        gpsInfoViewModel.getGpsStatusMutableLiveData().observeForever(gpsStatusObserver);

        assertNotNull(gpsInfoViewModel.getGpsDataMutableLiveData());
        assertNotNull(gpsInfoViewModel.getGpsStatusMutableLiveData());
        assertTrue(gpsInfoViewModel.getGpsStatusMutableLiveData().hasObservers());
        assertTrue(gpsInfoViewModel.getGpsDataMutableLiveData().hasObservers());
    }
}
