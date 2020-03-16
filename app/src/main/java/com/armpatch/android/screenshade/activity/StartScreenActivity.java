package com.armpatch.android.screenshade.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.service.OverlayService;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import static com.armpatch.android.screenshade.service.OverlayService.FILTER;

public class StartScreenActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_CODE = 1;
    private Intent serviceIntent;
    private BroadcastReceiver broadcastReceiver;
    private Button enableButton;
    private View buttonRevealPane;
    ObjectAnimator paneRevealAnimator;

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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean serviceDestroyed = intent.getBooleanExtra(OverlayService.EXTRA_SERVICE_DESTROYED, false);
                if (serviceDestroyed) {
                    enableButton();
                }
            }
        };
        setupRevealPane();

        TapTarget target = TapTarget.forView(enableButton, "Click here to show floating button")
                .outerCircleColor(R.color.dark_blue)
                .transparentTarget(true);


        TapTargetView.showFor(this, target, new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                if (enableButton.isEnabled()) enableButton.performClick();
            }
        });
    }

    private void setupRevealPane() {
        buttonRevealPane = findViewById(R.id.button_reveal_pane);
        buttonRevealPane.setScaleY(0.0f);
        buttonRevealPane.setPivotY(150);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);
        paneRevealAnimator = ObjectAnimator.ofPropertyValuesHolder(buttonRevealPane, scaleY);
        paneRevealAnimator.setInterpolator(new DecelerateInterpolator());
        paneRevealAnimator.setDuration(300);

        PropertyValuesHolder scaleY2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f);
        final ObjectAnimator paneHideAnimator = ObjectAnimator.ofPropertyValuesHolder(buttonRevealPane, scaleY2);
        paneHideAnimator.setDuration(300);
        paneHideAnimator.setStartDelay(400);
        paneHideAnimator.setInterpolator(new DecelerateInterpolator());

        paneRevealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                serviceIntent = OverlayService.getIntent(StartScreenActivity.this);
                startService(serviceIntent);
                disableButton();
                paneHideAnimator.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(FILTER);
        this.registerReceiver(broadcastReceiver, intentFilter);
        resetEnableButton();
    }

    private void resetEnableButton() {
        if (OverlayService.getInstance() == null) {
            enableButton();
        }
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
            paneRevealAnimator.start();
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

    private void enableButton() {
        enableButton.setEnabled(true);
    }

    private void disableButton() {
        enableButton.setEnabled(false);
    }
}
