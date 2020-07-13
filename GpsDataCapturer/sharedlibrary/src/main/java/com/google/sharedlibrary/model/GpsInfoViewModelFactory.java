package com.google.sharedlibrary.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * The view model factory class creates the instance of {@link }GpsInfoViewModel}
 */
public class GpsInfoViewModelFactory implements ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given {@code Class}.
     * <p>
     *
     * @param modelClass a {@code Class} whose instance is requested
     * @return a newly created ViewModel
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        Log.d("ViewModelFactory", "Create GpsInfoViewModelFactory");
        return (T) new GpsInfoViewModel();
    }
}
