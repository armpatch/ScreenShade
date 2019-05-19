package com.armpatch.android.secretscreen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import static android.view.View.inflate;

public class OverlayService extends Service {

    // Variables
    private Context context;
    private WindowManager mWindowManager;
    private static int windowParamType;
    private int displayHeight;
    private int displayWidth;

    // UI
    private View mOverlayControlsView;
    private Button mShowOverlayButton;
    private Button mHideOverlayButton;
    private ImageView mTopWindowBar;
    private ImageView mBottomWindowBar;

    static {
        if (Build.VERSION.SDK_INT >= 26) {
            windowParamType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowParamType = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    // Methods
    public static Intent getIntent(Context context) {
        return new Intent(context, OverlayService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplication();
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showOverlayControls();

        return START_STICKY;
    }

    private void showOverlayControls() {
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowParamType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowParams.gravity = Gravity.BOTTOM | Gravity.LEFT;

        mOverlayControlsView = inflate(context, R.layout.layout_overlay_controls, null);
        mWindowManager.addView(mOverlayControlsView, windowParams);

        mShowOverlayButton = mOverlayControlsView.findViewById(R.id.show_overlay_button);
        mShowOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mHideOverlayButton = mOverlayControlsView.findViewById(R.id.hide_overlay_button);
        mHideOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void showOverlay() {

    }

    private void setDisplayHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
