package com.armpatch.android.secretscreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.Resources;
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

    private Service rootService;
    private WindowManager windowManager;

    private ScreenBlocker screenBlocker;

    private int displayHeight;
    private int windowLayoutType;
    private int barLowerBounds;
    private int navBarHeight;
    private int statusBarHeight;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        rootService = service;
        windowManager = (WindowManager) rootService.getSystemService(Service.WINDOW_SERVICE);
        setDisplayVariables();

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

    private void setDisplayVariables() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;

        setNavBarHeight();
        setStatusBarHeight();

        barLowerBounds = displayHeight + statusBarHeight - navBarHeight;

    }

    private void setNavBarHeight() {
        Resources resources = rootService.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            navBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            navBarHeight = 0;
        }
    }

    public void setStatusBarHeight() {
        Resources resources = rootService.getResources();
        int resourceId = resources.getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = 0;
        }
    }

    class ScreenBlocker {

        WindowManager.LayoutParams layoutParams;
        int INITIAL_BAR_POS_Y = 1600;

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

            viewBarLayout = viewOverlay.findViewById(R.id.bar_layout);
            viewBarLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = event.getActionMasked();

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            lastTouchY = event.getRawY();
                            dimShade(true);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            final float y = event.getRawY();
                            final float dy = y - lastTouchY;
                            Log.v(TAG, "dy = " + dy);
                            moveBlockerPosY(dy);
                            updateWindowViewLayouts();

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

            viewShade.getLayoutParams().height = displayHeight + 500;
            layoutParams.y = INITIAL_BAR_POS_Y;
            layoutParams.height = displayHeight + 500;
        }

        void setBlockerPosY(int y) {
            int newY;

            if (y <= 0) {
                newY = 0;
            } else if (y > barLowerBounds) {
                newY = barLowerBounds;
            } else {
                newY = y;
            }
            layoutParams.y = newY;
        }

        void moveBlockerPosY(float dy){
            setBlockerPosY((int) (layoutParams.y + dy));
        }

        void dimShade(boolean makeDim) {
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
