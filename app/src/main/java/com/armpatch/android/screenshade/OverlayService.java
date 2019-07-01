package com.armpatch.android.screenshade;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class OverlayService extends Service {

    com.armpatch.android.screenshade.OverlayManager OverlayManager;
    private boolean overlayManagerStarted;

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
        if (!overlayManagerStarted){
            OverlayManager.start();
            overlayManagerStarted = true;
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
        OverlayManager.stop();
        super.onDestroy();
    }
}
