package com.armpatch.android.secretscreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

class OverlayManager {

    private static final String TAG = "OverlayManager";

    private Service rootService;
    private WindowManager windowManager;

    private ScreenBlocker screenBlocker;

    private int displayHeight;
    private int windowLayoutType;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }

        rootService = service;
        windowManager = (WindowManager) rootService.getSystemService(Service.WINDOW_SERVICE);
        setDisplayVariables();

        screenBlocker = new ScreenBlocker(R.layout.ui_plain_dark_shade);
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

    private void setDisplayVariables() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
    }

    class ScreenBlocker {

        WindowManager.LayoutParams layoutParams;
        int INITIAL_BAR_POS_Y = 1600;

        View viewOverlay, viewShade;

        @SuppressLint("ClickableViewAccessibility")
        ScreenBlocker(@LayoutRes int resource) {
            layoutParams = getDefaultLayoutParams();
            setViews(resource);
            setInitialHeights();
            }

        void setViews(@LayoutRes int resource) {
            viewOverlay = View.inflate(rootService, resource, null);

            viewShade = viewOverlay.findViewById(R.id.shade);

            viewOverlay.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }

        void setInitialHeights() {
            viewShade.getLayoutParams().height = displayHeight + 500;
            layoutParams.y = INITIAL_BAR_POS_Y;
            layoutParams.height = displayHeight + 500;
        }

        WindowManager.LayoutParams getDefaultLayoutParams() {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.type = windowLayoutType;
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.TOP;

            return params;
        }

        void setBlockerPosY(int y) {
            layoutParams.y = y;
        }

        void moveBlockerPosY(float dy){
            setBlockerPosY((int) (layoutParams.y + dy));
        }


    }
}
