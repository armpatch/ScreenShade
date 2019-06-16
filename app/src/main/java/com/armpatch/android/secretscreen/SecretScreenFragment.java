package com.armpatch.android.secretscreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SecretScreenFragment extends Fragment {

    private static final int REQUEST_OVERLAY_CODE = 2;

    // variables
    private Context context;

    private Intent serviceIntent;
    private ComponentName serviceComponent;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        context = inflater.getContext();

        View view = inflater.inflate(R.layout.fragment_intro_screen, container, false );

        initButtons(view);
        return view;
    }

    private void initButtons(View v) {
        // ui
        Button startServiceButton = v.findViewById(R.id.start_service_button);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptServiceStart();
            }
        });

        Button stopServiceButton = v.findViewById(R.id.stop_service_button);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptServiceStop();
            }
        });

    }

    private void attemptServiceStart() {
        if (!Settings.canDrawOverlays((context))) {
            requestPermission();
        } else if (!isServiceRunning()) {
            startService();
            //closeActivity();
        }

    }

    private void startService() {
        serviceIntent = OverlayService.getIntent(context);
        serviceComponent = context.startService(serviceIntent);
    }

    private boolean isServiceRunning() {
        return !(serviceComponent == null);
    }

    private void requestPermission() {
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        startActivityForResult(i, REQUEST_OVERLAY_CODE );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!Settings.canDrawOverlays((context))) {
            //create dialog that asks to enable permission
            Toast toast = Toast.makeText(context, R.string.permission_denied_toast, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0,200);
            toast.show();
        }
    }

    private void attemptServiceStop() {
        if (serviceIntent != null) {
            stopService();
            serviceComponent = null;
        }
    }

    private void stopService() {
        context.stopService(serviceIntent);
    }

    private void closeActivity() {
        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
