package com.android.tamzeveloper.myweather.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by tahmina on 15-04-23.
 */
public class MyWeatherSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MyWeatherSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
       // Log.d("TAMZ", "onCreate - SyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new MyWeatherSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}