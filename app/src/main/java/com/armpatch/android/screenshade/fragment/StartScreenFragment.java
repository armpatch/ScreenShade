package com.armpatch.android.screenshade.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.service.OverlayService;

public class StartScreenFragment extends Fragment {

    private static final int REQUEST_OVERLAY_CODE = 2;

    // variables
    private Context appContext;
    private Intent serviceIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        appContext = inflater.getContext();

        View view = inflater.inflate(R.layout.fragment_start_screen, container, false );

        initButtons(view);
        return view;
    }

    @Override
    public void onResume() {
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
        int DELAYED_START_TIME = 500;
        handler.postDelayed(runnable, DELAYED_START_TIME);
    }

    private void initButtons(View v) {
        Button feedbackButton = v.findViewById(R.id.send_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailFeedback();
            }
        });
    }

    private void attemptToStartService() {
        if (!Settings.canDrawOverlays((appContext))) {
            requestPermission();
        } else {
            serviceIntent = OverlayService.getIntent(appContext);
            appContext.startService(serviceIntent);
        }
    }

    private void requestPermission() {
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + appContext.getPackageName()));
        startActivityForResult(i, REQUEST_OVERLAY_CODE );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!Settings.canDrawOverlays((appContext))) {

            //create dialog that asks to enable permission
            Toast toast = Toast.makeText(appContext, R.string.permission_denied_toast, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0,200);
            toast.show();
        }
    }

    private void sendEmailFeedback() {
        final String[] myEmail = {"aaronpatch.developer@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, myEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScreenShade - Feedback");

        if (emailIntent.resolveActivity(appContext.getPackageManager()) != null) {
            appContext.startActivity(emailIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appContext.stopService(serviceIntent);
    }
}
