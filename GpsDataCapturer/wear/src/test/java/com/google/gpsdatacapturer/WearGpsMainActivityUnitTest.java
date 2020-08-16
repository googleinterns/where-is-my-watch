package com.google.gpsdatacapturer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.gpsdatacapturer.databinding.ActivityWearGpsMainBinding;
import com.google.sharedlibrary.model.GpsData;
import com.google.sharedlibrary.model.GpsInfoViewModel;
import com.google.sharedlibrary.service.GpsDataCaptureService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class WearGpsMainActivityUnitTest {
    private WearGpsMainActivity wearGpsMainActivity;

    @Mock
    private GpsDataCaptureService gpsDataCaptureService;
    @Mock
    private GpsInfoViewModel gpsInfoViewModel;

    private Button startAndStopButton;
    private RadioGroup apiRadioGroup;
    private TextView gpsDataTextView;
    private TextView gpsStatusTextView;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
        ActivityController<WearGpsMainActivity> activityController = Robolectric.buildActivity(
                WearGpsMainActivity.class);
        wearGpsMainActivity = activityController.get();

        //Set up the service and binder
        ShadowApplication application = ShadowApplication.getInstance();

        gpsDataCaptureService = mock(GpsDataCaptureService.class);
        GpsDataCaptureService.GpsDataCaptureBinder binder = mock(
                GpsDataCaptureService.GpsDataCaptureBinder.class);

        when(binder.getService()).thenReturn(gpsDataCaptureService);

        application.setComponentNameAndServiceForBindService(
                new ComponentName(wearGpsMainActivity, GpsDataCaptureService.class), binder);

        //Enable GPS Provider
        LocationManager locationManager = (LocationManager) wearGpsMainActivity.getSystemService(
                Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = Shadows.shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true);

        activityController.create();
    }

    @Test
    public void testWearGpsMainActivityNotNull() {
        assertNotNull(wearGpsMainActivity);
    }

    //The following test testing UI behaves as expected
    @Test
    public void testInitialUIShowingAsExpected() {
        apiRadioGroup = wearGpsMainActivity.findViewById(R.id.radio_group_location_api);
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
        gpsDataTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_data);
        gpsStatusTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_status);

        assertNotNull(apiRadioGroup);
        assertNotNull(startAndStopButton);
        assertNotNull(gpsDataTextView);
        assertNotNull(gpsStatusTextView);

        assertEquals(View.VISIBLE, apiRadioGroup.getVisibility());
        assertEquals(View.VISIBLE, startAndStopButton.getVisibility());
        assertEquals("START", startAndStopButton.getText());
        assertEquals(View.GONE, gpsDataTextView.getVisibility());
        assertEquals(View.GONE, gpsStatusTextView.getVisibility());
    }

    @Test
    public void testLmRadioButtonChecked() {
        apiRadioGroup = wearGpsMainActivity.findViewById(R.id.radio_group_location_api);
        RadioButton lmRadioButton = wearGpsMainActivity.findViewById(R.id.radio_button_LM);
        assertNotNull(lmRadioButton);
        lmRadioButton.performClick();
        assertEquals(R.id.radio_button_LM, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testFlpRadioButtonChecked() {
        apiRadioGroup = wearGpsMainActivity.findViewById(R.id.radio_group_location_api);
        RadioButton flpRadioButton = wearGpsMainActivity.findViewById(R.id.radio_button_FLP);
        assertNotNull(flpRadioButton);
        flpRadioButton.performClick();
        assertEquals(R.id.radio_button_FLP, apiRadioGroup.getCheckedRadioButtonId());
    }

    @Test
    public void testStartButtonClickedUIChangedAsExpected() {
        apiRadioGroup = wearGpsMainActivity.findViewById(R.id.radio_group_location_api);
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
        gpsDataTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_data);
        gpsStatusTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_status);

        startAndStopButton.performClick();

        assertEquals(View.GONE, apiRadioGroup.getVisibility());
        assertEquals("STOP", startAndStopButton.getText());
        assertEquals(View.VISIBLE, gpsDataTextView.getVisibility());
        assertEquals(View.VISIBLE, gpsStatusTextView.getVisibility());
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
        assertEquals(gpsInfoViewModel, binding.getGpsInfoViewModel());
        assertEquals(wearGpsMainActivity, binding.getLifecycleOwner());
    }

    //The following test testing gpsDataCapture service start as expected
    @Test
    public void testGpsDataCaptureServiceStarted() {
        ShadowLog.stream = System.out;
        Intent expectedIntent = new Intent(wearGpsMainActivity, GpsDataCaptureService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(wearGpsMainActivity);

        Intent shadowIntent = shadowActivity.getNextStartedService();

        assertTrue(shadowIntent.filterEquals(expectedIntent));
    }

    @Test
    public void testStartGpsCaptureOnStartButtonClicked() {
        ShadowLog.stream = System.out;
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
        startAndStopButton.performClick();

        verify(gpsDataCaptureService).setGpsInfoViewModel(any());
        verify(gpsDataCaptureService).startCapture();
    }


    @Test
    public void testStopGpsCaptureOnStopButtonClicked() {
        ShadowLog.stream = System.out;
        startAndStopButton = wearGpsMainActivity.findViewById(R.id.button_start_stop);
        startAndStopButton.performClick();
        startAndStopButton.performClick();

        verify(gpsDataCaptureService).stopCapture();
    }

    @Test
    public void testGpsDataTextViewUpdateAsExpected() {
        //Given
        Location loc = mock(Location.class);

        gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsInfoViewModel.setGpsDataMutableLiveData(loc);

        MutableLiveData<GpsData> gpsDataLiveData = new MutableLiveData<>();
        gpsDataLiveData.setValue(new GpsData(loc));

        //When
        when(gpsInfoViewModel.getGpsDataMutableLiveData()).thenReturn(gpsDataLiveData);
        gpsDataTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_data);
        Observer<GpsData> gpsDataObserver = new Observer<GpsData>() {
            @Override
            public void onChanged(GpsData gpsData) {
                gpsDataTextView.setText(gpsData.getGpsDataString());
            }
        };
        gpsInfoViewModel.getGpsDataMutableLiveData().observeForever(
                gpsDataObserver);

        //Then
        String data = gpsInfoViewModel.getGpsDataMutableLiveData().getValue().getGpsDataString();
        assertEquals(data, gpsDataTextView.getText().toString());
    }

    @Test
    public void testGpsStatusTextViewUpdateAsExpected() {
        //Given
        String gpsStatus = "Gps Status: GPS_EVENT_STARTED";
        gpsInfoViewModel = mock(GpsInfoViewModel.class);
        gpsInfoViewModel.setGpsStatusMutableLiveData(1);
        MutableLiveData<String> gpsStatusLiveData = new MutableLiveData<>();
        gpsStatusLiveData.setValue(gpsStatus);

        //When
        when(gpsInfoViewModel.getGpsStatusMutableLiveData()).thenReturn(gpsStatusLiveData);
        gpsStatusTextView = wearGpsMainActivity.findViewById(R.id.text_view_gps_status);
        Observer<String> gpsStatusObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                gpsStatusTextView.setText(s);
            }
        };
        gpsInfoViewModel.getGpsStatusMutableLiveData().observeForever(
                gpsStatusObserver);

        //Then
        String status = gpsInfoViewModel.getGpsStatusMutableLiveData().getValue();
        assertEquals(status, gpsStatusTextView.getText().toString());
    }

    @After
    public void tearDown() {
        wearGpsMainActivity.onDestroy();
    }
}
