package com.armpatch.android.screenshade.fragments;

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

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.services.OverlayService;

public class ScreenShadeFragment extends Fragment {

    private static final int REQUEST_OVERLAY_CODE = 2;

    // variables
    private Context context; //TODO rename, this should not be called context, because it's to vague
    private Intent serviceIntent;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        context = inflater.getContext();

        View view = inflater.inflate(R.layout.start_screen_fragment, container, false );

        initButtons(view);
        return view;
    }

    private void initButtons(View v) {
        // ui
        Button startServiceButton = v.findViewById(R.id.start_button);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptServiceStart();
            }
        });

        Button feedbackButton = v.findViewById(R.id.send_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });
    }

    private void attemptServiceStart() {
        if (!Settings.canDrawOverlays((context))) {
            requestPermission();
        } else {
            startService();
            //closeActivity();
        }
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

    private void startService() {
        serviceIntent = OverlayService.getIntent(context);
        context.startService(serviceIntent);
    }

    private void stopOverlayService() {
        context.stopService(serviceIntent);
    }

    private void sendFeedback() {
        final String[] myEmail = {"aaronpatch.developer@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, myEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScreenShade - Feedback");

        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(emailIntent);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopOverlayService();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
