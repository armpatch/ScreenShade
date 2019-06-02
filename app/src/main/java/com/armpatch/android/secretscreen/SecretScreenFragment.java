package com.armpatch.android.secretscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SecretScreenFragment extends Fragment {

    // variables
    private Context fragmentContext;
    private Intent serviceIntent;

    // ui
    private Button startServiceButton;
    private Button stopServiceButton;
    private Button permissionButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentContext = inflater.getContext();

        View v = inflater.inflate(R.layout.fragment_start_screen, container, false );

        initButtons(v);
        return v;
    }

    private void initButtons(View v) {
        permissionButton = v.findViewById(R.id.button_ask_permission_draw_over);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + fragmentContext.getPackageName()));
                fragmentContext.startActivity(i);
            }
        });

        startServiceButton = v.findViewById(R.id.start_service_button);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOverlayService();
            }
        });

        stopServiceButton = v.findViewById(R.id.stop_service_button);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopOverlayService();
                quickToast("stop service attempted");
            }
        });

    }

    private void startOverlayService() {
        if (Settings.canDrawOverlays(fragmentContext)) {
            serviceIntent = OverlayService.getIntent(fragmentContext);
            fragmentContext.startService(serviceIntent);
        } else {
            quickToast("ask permission first.");
        }
    }

    private void stopOverlayService() {
        if (serviceIntent != null)
            fragmentContext.stopService(serviceIntent);
    }

    public void quickToast(String message) {
        Toast.makeText(fragmentContext, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopOverlayService();
    }
}
