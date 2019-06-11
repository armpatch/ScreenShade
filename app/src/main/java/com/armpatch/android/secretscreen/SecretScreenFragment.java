package com.armpatch.android.secretscreen;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.ACTIVITY_SERVICE;

public class SecretScreenFragment extends Fragment {

    // variables
    private Context fragmentContext;

    private Intent serviceIntent;
    private ComponentName serviceComponent;


    // ui
    private Button startServiceButton, stopServiceButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentContext = inflater.getContext();

        View view = inflater.inflate(R.layout.fragment_intro_screen, container, false );

        initButtons(view);
        return view;
    }

    private void initButtons(View v) {
        startServiceButton = v.findViewById(R.id.start_service_button);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptServiceStart();
            }
        });

        stopServiceButton = v.findViewById(R.id.stop_service_button);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptServiceStop();
            }
        });

    }

    private void attemptServiceStart() {
        if (Settings.canDrawOverlays(fragmentContext) && !isServiceRunning()) {
            startService();
        } else {
            requestPermission();
        }
    }

    private boolean isServiceRunning() {
        if (serviceComponent == null) {
            return false;
        } else if (serviceComponent.getClassName())
        return ;

    }

    private void startService() {
        serviceIntent = OverlayService.getIntent(fragmentContext);
        serviceComponent = fragmentContext.startService(serviceIntent);
    }

    private void attemptServiceStop() {
        if (serviceIntent != null)
            fragmentContext.stopService(serviceIntent);
    }

    private void requestPermission() {
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + fragmentContext.getPackageName()));
        fragmentContext.startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
