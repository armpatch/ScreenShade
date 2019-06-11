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
import android.widget.ImageButton;

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
    private int barLowerBounds;
    private int navBarHeight;
    private int statusBarHeight;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
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

        navBarHeight = getNavBarHeight();
        statusBarHeight = getStatusBarHeight();

        barLowerBounds = displayHeight + statusBarHeight - navBarHeight;
    }

    private int getNavBarHeight() {
        int height;

        Resources resources = rootService.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId);
        } else {
            height = 0;
        }

        return height;
    }

    private int getStatusBarHeight() {
        int height;

        Resources resources = rootService.getResources();
        int resourceId = resources.getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId);
        } else {
            height = 0;
        }

        return height;
    }

    class ScreenBlocker {

        WindowManager.LayoutParams layoutParams;
        int INITIAL_BAR_POS_Y = 1600;

        View viewOverlay, viewBarLayout, viewBar, viewShade;
        ImageButton buttonExit;

        float lastTouchY;

        @SuppressLint("ClickableViewAccessibility")
        ScreenBlocker(@LayoutRes int resource) {
            layoutParams = getDefaultLayoutParams();
            setViews(resource);
            setInitialHeights();
            }

        void setViews(@LayoutRes int resource) {
            viewOverlay = View.inflate(rootService, resource, null);

            viewBarLayout = viewOverlay.findViewById(R.id.bar_layout);
            viewBarLayout.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
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
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            buttonExit = viewOverlay.findViewById(R.id.exit_button);
            buttonExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OverlayService overlayService = (OverlayService) rootService;
                    overlayService.onDestroy();
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

        void elevateBar() { // raise elevation of bar as it moves

        }
    }
}
