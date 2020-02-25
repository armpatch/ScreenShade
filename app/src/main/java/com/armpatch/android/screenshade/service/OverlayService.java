package com.armpatch.android.screenshade.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.armpatch.android.screenshade.overlay.OverlayManager;

public class OverlayService extends Service {

    public static OverlayService overlayService;
    public static final String FILTER = "com.armpatch.android.screenshade.service.OverlayService";

    OverlayManager overlayManager;
    private boolean isRunning;
    public static final String EXTRA_SERVICE_DESTROYED = "EXTRA_SERVICE_DESTROYED";

    public static Intent getIntent(Context context) {
        return new Intent(context, OverlayService.class);
    }

    public static OverlayService getInstance() {
        if (overlayService != null) {
            return overlayService;
        } else {
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        overlayManager = new OverlayManager(this);
        overlayService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning){
            overlayManager.showButton();
            isRunning = true;
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        overlayService = null;
        overlayManager.removeAll();
        broadcastThisServiceIsDestroyed();
        super.onDestroy();
    }

    private void broadcastThisServiceIsDestroyed() {
        Intent i = new Intent();
        i.putExtra(EXTRA_SERVICE_DESTROYED, true);
        i.setAction(FILTER);
        sendBroadcast(i);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("OverlayService", "landscape");
            overlayManager.toggleOrientationChange();
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("OverlayService", "portrait");
        }
    }
}
