package com.armpatch.android.screenshade.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.service.OverlayService;

public class StartScreenActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_CODE = 1;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        delayedStart();
    }

    private void delayedStart() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                attemptToStartService();
            }
        };
        int DELAYED_START_TIME = 0;
        handler.postDelayed(runnable, DELAYED_START_TIME);
    }

    private void attemptToStartService() {
        if (!Settings.canDrawOverlays((this))) {
            requestOverlayPermission();
        } else {
            serviceIntent = OverlayService.getIntent(this);
            startService(serviceIntent);
        }
    }

    private void requestOverlayPermission() {
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(i, REQUEST_OVERLAY_CODE );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!Settings.canDrawOverlays((this))) {

            Toast toast = Toast.makeText(this, R.string.permission_denied_toast, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0,200);
            toast.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
    }

}
