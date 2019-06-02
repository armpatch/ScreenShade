package com.armpatch.android.secretscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class OverlayService extends Service {

    com.armpatch.android.secretscreen.OverlayManager OverlayManager;
    Context baseContext;

    // Methods
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
        OverlayManager.start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast exitToast = Toast.makeText(getApplication(),
                "onDestroy:  Overlay Service",Toast.LENGTH_SHORT);
        exitToast.setGravity(Gravity.TOP, 0,0);
        exitToast.show();

        OverlayManager.stop();


        super.onDestroy();
    }
}
