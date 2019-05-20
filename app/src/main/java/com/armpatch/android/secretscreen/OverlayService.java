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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import static android.view.View.inflate;

public class OverlayService extends Service {

    // Variables
    private Context context;
    private WindowManager mWindowManager;
    private static int windowLayoutType;
    private int displayHeight;
    private int displayWidth;

    // UI
    private View mOverlayControlsView;
    private Button mShowOverlayButton;
    private Button mHideOverlayButton;
    private View mTopDraggableBorder;
    private View mBottomDraggableBorder;

    static {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
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
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowParams.gravity = Gravity.BOTTOM | Gravity.LEFT;

        mOverlayControlsView = inflate(context, R.layout.ui_window_controls, null);
        mWindowManager.addView(mOverlayControlsView, windowParams);

        mShowOverlayButton = mOverlayControlsView.findViewById(R.id.show_overlay_button);
        mShowOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverlay();
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
        final WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowParams.gravity = Gravity.LEFT;

        mBottomDraggableBorder = inflate(context, R.layout.ui_window_border, null);

        windowParams.x = 0;
        windowParams.y = 100;

        mWindowManager.addView(mBottomDraggableBorder, windowParams);
        mBottomDraggableBorder.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x;
                        initialY = windowParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move ImageView
                        windowParams.x = initialX + (int) (initialTouchX - event.getRawX());
                        windowParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mBottomDraggableBorder, windowParams);
                        return true;
                }
                return false;
            }
        });
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
