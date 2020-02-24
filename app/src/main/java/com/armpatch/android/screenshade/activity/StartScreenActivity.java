package com.armpatch.android.screenshade.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.service.OverlayService;

import static com.armpatch.android.screenshade.service.OverlayService.FILTER;

public class StartScreenActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_CODE = 1;
    private Intent serviceIntent;
    BroadcastReceiver broadcastReceiver;
    Button enableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        enableButton = findViewById(R.id.show_controls_button);
        enableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToStartService();
            }
        });

        View contentView = this.findViewById(android.R.id.content);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceIntent == null) {
                    enableButton.setEnabled(true);
                }
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean serviceDestroyed = intent.getBooleanExtra(OverlayService.EXTRA_SERVICE_DESTROYED, false);
                if (serviceDestroyed) {
                    enableButton.setEnabled(true);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(FILTER);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void attemptToStartService() {
        if (!Settings.canDrawOverlays((this))) {
            requestOverlayPermission();
        } else {
            serviceIntent = OverlayService.getIntent(this);
            startService(serviceIntent);
            enableButton.setEnabled(false);
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
