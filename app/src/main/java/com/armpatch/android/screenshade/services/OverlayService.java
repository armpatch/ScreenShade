package com.armpatch.android.screenshade.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.armpatch.android.screenshade.overlays.OverlayManager;

public class OverlayService extends Service {

    com.armpatch.android.screenshade.overlays.OverlayManager OverlayManager;
    private boolean hasBeenStarted;

    public static Intent getIntent(Context context) {
        return new Intent(context, OverlayService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OverlayManager = new OverlayManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!hasBeenStarted){
            OverlayManager.revealMovableButton();
            hasBeenStarted = true;
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
        OverlayManager.hideAllOverlays();
        super.onDestroy();
    }
}
