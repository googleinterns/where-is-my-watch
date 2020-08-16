package com.google.sharedlibrary;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.google.sharedlibrary.storage.GpxFileFolder;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class GpxFileFolderUnitTest {
    @Mock
    private File gpxFileFolder;

    @Mock
    private Context context;

    @Test
    public void testCreateGpxFileFolder(){
        ShadowLog.stream = System.out;
        //Given
        gpxFileFolder = mock(File.class);
        context = mock(Context.class);
        when(context.getExternalFilesDir(Environment.getExternalStorageDirectory().getAbsolutePath())).thenReturn(gpxFileFolder);

        //When
        GpxFileFolder.createGpsDataFolder(context);

        //Then
        verify(context).getExternalFilesDir((String) any());
        assertNotNull(gpxFileFolder);
    }
}
