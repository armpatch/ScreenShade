package com.armpatch.android.secretscreen;

import android.annotation.SuppressLint;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

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
    private View mBottomScreenBlockView;

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

    @SuppressLint("ClickableViewAccessibility")
    private void showOverlay() {
        final WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowParams.gravity = Gravity.LEFT;

        mBottomScreenBlockView = inflate(context, R.layout.ui_screen_block_bottom, null);
        final ImageView viewBlocker = mBottomScreenBlockView.findViewById(R.id.view_blocker);

        final ViewGroup.LayoutParams viewBlockerParams = viewBlocker.getLayoutParams();

        windowParams.x = 0;
        windowParams.y = 0;

        ImageView dragBar = mBottomScreenBlockView.findViewById(R.id.drag_bar);

        mWindowManager.addView(mBottomScreenBlockView, windowParams);
        dragBar.setOnTouchListener(new View.OnTouchListener() {
            private int initialLayoutHeight;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialLayoutHeight = viewBlocker.getHeight();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // move ImageView
                        viewBlockerParams.height = initialLayoutHeight - (int) (event.getRawY() - initialTouchY);
                        // windowParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mBottomScreenBlockView, windowParams);
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
