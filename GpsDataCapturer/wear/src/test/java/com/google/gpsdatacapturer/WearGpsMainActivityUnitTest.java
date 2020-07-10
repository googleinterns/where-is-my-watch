package com.google.gpsdatacapturer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.text.Layout;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.gpsdatacapturer.databinding.ActivityWearGpsMainBinding;
import com.google.sharedlibrary.model.GpsData;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.model.GpsInfoViewModelFactory;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowService;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class WearGpsMainActivityUnitTest {
    private WearGpsMainActivity wearGpsMainActivity;
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
        wearGpsMainActivity = Robolectric.buildActivity(WearGpsMainActivity.class).create().get();
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
        apiRadioGroup = wearGpsMainActivity.findViewById(R.id.radio_group_location_api);
        lmRadioButton = wearGpsMainActivity.findViewById(R.id.radio_button_LM);
        flpRadioButton = wearGpsMainActivity.findViewById(R.id.radio_button_FLP);
        gpsDataTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_data);
        gpsStatusTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_status);
    }

    @Test
    public void testWearGpsMainActivityNotNull() {
        assertNotNull(wearGpsMainActivity);
    }

    //The following test testing UI behaviors as expected
    @Test
    public void testUIComponentsNotNull() {
        assertNotNull(startAndStopButton);
        assertNotNull(apiRadioGroup);
        assertNotNull(lmRadioButton);
        assertNotNull(flpRadioButton);
        assertNotNull(gpsDataTextView);
        assertNotNull(gpsStatusTextView);

        View layout = LayoutInflater.from(wearGpsMainActivity).inflate(
                R.layout.activity_wear_gps_main,
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
        assertEquals(R.id.radio_button_LM, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testFlpRadioButtonChecked() {
        flpRadioButton.performClick();
        assertEquals(R.id.radio_button_FLP, apiRadioGroup.getCheckedRadioButtonId());
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
        ActivityWearGpsMainBinding binding =
                DataBindingUtil.setContentView(wearGpsMainActivity,
                        R.layout.activity_wear_gps_main);
        binding.setGpsInfoViewModel(gpsInfoViewModel);
        binding.setLifecycleOwner(wearGpsMainActivity);
        assertNotNull(binding);
        assertEquals(binding.getGpsInfoViewModel(), gpsInfoViewModel);
        assertEquals(binding.getLifecycleOwner(), wearGpsMainActivity);
    }

    //The following test testing gpsDataCapture service start and stop as expected
    @Test
    public void testStartButtonClickShouldStartService() {
        lmRadioButton.performClick();
        startAndStopButton.performClick();

        Intent expectedIntent = new Intent(wearGpsMainActivity, GpsDataCaptureService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(wearGpsMainActivity);
        Intent shadowIntent = shadowActivity.getNextStartedService();

        assertTrue(shadowIntent.filterEquals(expectedIntent));
    }

    @Test
    public void testStopButtonClickShouldStopService() {
        GpsDataCaptureService service = mock(GpsDataCaptureService.class);

        Intent intent = new Intent(wearGpsMainActivity, GpsDataCaptureService.class);
        GpsDataCaptureService.GpsDataCaptureBinder binder = mock(GpsDataCaptureService.GpsDataCaptureBinder.class);
        when(binder.getService()).thenReturn(service);
        startAndStopButton.performClick();
        wearGpsMainActivity.gpsServiceConnection.onServiceConnected(new ComponentName(wearGpsMainActivity, GpsDataCaptureService.class), binder);

        startAndStopButton.performClick();
        ShadowActivity shadowActivity = Shadows.shadowOf(wearGpsMainActivity);
        Intent shadowIntent = shadowActivity.getNextStoppedService();
        assertTrue(shadowIntent.filterEquals(intent));

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
