package com.google.sharedlibrary.model;

import android.location.GpsStatus;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class GpsInfoViewModel extends ViewModel {
    private static final String TAG = "GpsInfoViewModel";
    private MutableLiveData<GpsData> gpsDataMutableLiveData;
    private MutableLiveData<String> gpsStatusMutableLiveData;

    public GpsInfoViewModel() {
        Log.i("ViewModel", "Create GpsInfoViewModel");
        gpsDataMutableLiveData = new MutableLiveData<>();
        gpsStatusMutableLiveData = new MutableLiveData<>();
    }

    /**
     * @param location location passed from location listener
     */
    public void setGpsDataMutableLiveData(Location location) {
        Log.d(TAG, "setGpsDataMutableLiveData");
        gpsDataMutableLiveData.setValue(new GpsData(location));
    }

    /**
     * @param event event passed from gps status listener
     */
    public void setGpsStatusMutableLiveData(int event) {
        Log.d(TAG, "setGpsStatusMutableLiveData");
        switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                gpsStatusMutableLiveData.setValue("Gps Status: GPS_EVENT_FIRST_FIX");
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                gpsStatusMutableLiveData.setValue("Gps Status: GPS_EVENT_STARTED");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                gpsStatusMutableLiveData.setValue("Gps Status: GPS_EVENT_STOPPED");
                break;
        }
    }

    /**
     * @return gpsDataMutableLiveData
     */
    @NonNull
    public LiveData<GpsData> getGpsDataMutableLiveData() {
        Log.d(TAG, "getGpsDataMutableLiveData");
        return gpsDataMutableLiveData;
    }

    /**
     * @return gpsStatusMutableLiveData
     */
    @NonNull
    public LiveData<String> getGpsStatusMutableLiveData() {
        Log.d(TAG, "getGpsStatusMutableLiveData");
        return gpsStatusMutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
