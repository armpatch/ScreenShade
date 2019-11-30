package com.armpatch.android.screenshade.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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

        Button showControlsButton = findViewById(R.id.show_controls_button);
        showControlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToStartService();
            }
        });
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

        if (requestCode == REQUEST_OVERLAY_CODE) {
            if (Settings.canDrawOverlays((this))) {
                attemptToStartService();
            } else {
                Toast.makeText(this, R.string.permission_denied_toast, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceIntent != null)
            stopService(serviceIntent);
    }
}
