package com.google.sharedlibrary.model;

import android.location.GpsStatus;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The GpsInfoViewModel class will fetch Gps data and status from the GpsDataCaptureService and pass them to the UI
 */
public class GpsInfoViewModel extends ViewModel {
    private static final String TAG = "GpsInfoViewModel";
    private MutableLiveData<GpsData> gpsDataMutableLiveData;
    private MutableLiveData<String> gpsStatusMutableLiveData;
    private MutableLiveData<String>  satellitesUsedInFix;
    private MutableLiveData<Boolean> isGpsDataAvailable;

    public GpsInfoViewModel() {
        Log.i("ViewModel", "Create GpsInfoViewModel");
        gpsDataMutableLiveData = new MutableLiveData<>();
        gpsStatusMutableLiveData = new MutableLiveData<>();
        satellitesUsedInFix = new MutableLiveData<>();
        isGpsDataAvailable = new MutableLiveData<>();

    }

    /**
     * Set the GpsDataMutableLiveData value with the updated location
     * @param location location passed from location listener
     */
    public void setGpsDataMutableLiveData(Location location) {
        Log.d(TAG, "setGpsDataMutableLiveData");
        gpsDataMutableLiveData.setValue(new GpsData(location));
        isGpsDataAvailable.setValue(true);
    }

    /**
     * Set the GpsStatusMutableLiveData value according to the event
     * @param event event passed from gps status listener
     */
    public void setGpsStatusMutableLiveData(int event) {
        Log.d(TAG, "setGpsStatusMutableLiveData");
        switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                gpsStatusMutableLiveData.setValue("GPS_EVENT_FIRST_FIX");
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                gpsStatusMutableLiveData.setValue("GPS_EVENT_STARTED");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                gpsStatusMutableLiveData.setValue("GPS_EVENT_STOPPED");
                break;
        }
    }

    /**
     * Set the satellietesUsedInFix value
     * @param  satellites the number of satellites used in fix passed from gps status listener
     */
    public void setSatellitesUsedInFix(int satellites){
        if(satellites == -1){
            Log.d(TAG, "setSatellitesUsedInFix: UNKNOWN");
            satellitesUsedInFix.setValue("UNKNOWN");
        }
        Log.d(TAG, "setSatellitesUsedInFix");
        satellitesUsedInFix.setValue(String.valueOf(satellites));
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

    /**
     * @return satellitesUsedInFix
     */
    public LiveData<String> getSatellitesUsedInFix(){
        Log.d(TAG, "getSatellitesUsedInFix");
        return satellitesUsedInFix;
    }

    /**
     * @return if gps data available
     */
    public LiveData<Boolean> getGpsDataAvailability(){
        return isGpsDataAvailable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
