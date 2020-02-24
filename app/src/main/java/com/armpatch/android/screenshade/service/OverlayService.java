package com.armpatch.android.screenshade.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.armpatch.android.screenshade.overlay.OverlayManager;

public class OverlayService extends Service {

    com.armpatch.android.screenshade.overlay.OverlayManager overlayManager;
    private boolean isRunning;

    public static Intent getIntent(Context context) {
        return new Intent(context, OverlayService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        overlayManager = new OverlayManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning){
            overlayManager.showButtonOverlay();
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
        overlayManager.hideAllOverlays();
        super.onDestroy();
    }
}
