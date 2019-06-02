package com.armpatch.android.secretscreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;

class OverlayManager {

    public static final String TAG = "OverlayManager";

    // developer option
    private static final boolean loggingIsOn = true;

    private Service rootService;
    private WindowManager windowManager;

    private ScreenBlocker screenBlocker;

    private int displayHeight;
    private int windowLayoutType;


    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        rootService = service;
        windowManager = (WindowManager) rootService.getSystemService(Service.WINDOW_SERVICE);
        setDisplayMetrics();

        screenBlocker = new ScreenBlocker(R.layout.ui_blocker_layout);
    }

    void start() {
        addViews();
    }

    void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {
        windowManager.addView(screenBlocker.viewOverlay, screenBlocker.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(screenBlocker.viewOverlay, screenBlocker.layoutParams);
    }

    private void removeViews() {
        windowManager.removeView(screenBlocker.viewOverlay);
    }

    private void setDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
    }

    class ScreenBlocker {

        WindowManager.LayoutParams layoutParams;
        int INITIAL_BAR_POS_Y = 1000;

        View viewOverlay;
        View viewBarLayout;
        View viewBar;
        View viewShade;

        float lastTouchY;

        @SuppressLint("ClickableViewAccessibility")
        ScreenBlocker(@LayoutRes int resource) {
            viewOverlay = View.inflate(rootService, resource, null);

            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.type = windowLayoutType;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.TRANSPARENT;
            layoutParams.gravity = Gravity.TOP;

            // setup viewBar
            viewBarLayout = viewOverlay.findViewById(R.id.bar_layout);
            viewBarLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = event.getActionMasked();

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            lastTouchY = event.getRawY();
                            Log.v(TAG, "moved");
                            dimShade(true);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            final float y = event.getRawY();
                            final float dy = y - lastTouchY;
                            moveBlockerPosY(dy);

                            // Remember this touch position for the next move event
                            lastTouchY = y;
                            break;

                        case MotionEvent.ACTION_UP:
                            dimShade(false);
                            break;
                    }
                    return true;
                }
            });
            viewShade = viewOverlay.findViewById(R.id.shade);

            viewOverlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            setBlockerPosY(INITIAL_BAR_POS_Y);
        }

        void setBlockerPosY(int height) {
            layoutParams.y = height;

        }

        void moveBlockerPosY(float dy){

            layoutParams.y += dy;
            updateWindowViewLayouts();
        }

        void dimShade(boolean makeDim) {
            float alpha = 0.8f;
            if(makeDim) {
                viewShade.setBackgroundColor(rootService.getColor(R.color.color_shade_transparent));
                viewBarLayout.setBackgroundColor(rootService.getColor(R.color.color_shade_transparent));
            } else {
                viewShade.setBackgroundColor(rootService.getColor(R.color.color_shade_normal));
                viewBarLayout.setBackgroundColor(rootService.getColor(R.color.color_shade_normal));
            }
        }

        void elevateBar() {

        }
    }
}
