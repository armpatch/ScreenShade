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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;

class OverlayManager {

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
        windowManager.addView(screenBlocker.layoutView, screenBlocker.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(screenBlocker.layoutView, screenBlocker.layoutParams);
    }



    private void removeViews() {
        windowManager.removeView(screenBlocker.layoutView);
    }

    private void setDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
    }

    class ScreenBlocker {

        WindowManager.LayoutParams layoutParams;
        int layoutRes;

        LinearLayout layoutView;
        ImageView barView;
        ImageView shadeView;

        int barPosY;
        float lastTouchY;


        @SuppressLint("ClickableViewAccessibility")
        ScreenBlocker(@LayoutRes int resource) {
            setLayoutRes(resource);

            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.type = windowLayoutType;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.TRANSPARENT;
            layoutParams.gravity = Gravity.TOP;

            barView = layoutView.findViewById(R.id.bar);
            shadeView = layoutView.findViewById(R.id.shade);

            barView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = event.getActionMasked();

                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            lastTouchY = event.getRawY();
                            setTransparency(true);
                            break;

                        case MotionEvent.ACTION_MOVE:
                            final float y = event.getRawY();
                            final float dy = y - lastTouchY;
                            moveWindowVertically(dy);

                            // Remember this touch position for the next move event
                            lastTouchY = y;
                            break;

                        case MotionEvent.ACTION_UP:
                            setTransparency(false);
                            break;
                    }
                    return true;
                }
            });

            updateComponentParameters();
        }

        void setLayoutRes(@LayoutRes int resource) {
            layoutRes = resource;
            layoutView = (LinearLayout) View.inflate(rootService, layoutRes, null);
        }

        void updateComponentParameters() {
            layoutParams.y = barPosY;
            shadeView.getLayoutParams().height = displayHeight - layoutParams.y;
        }

        void setBlockerHeight(int height) {
            barPosY = height;
        }

        void moveWindowVertically(float dy){
            barPosY += dy;
            updateComponentParameters();
            updateWindowViewLayouts();
        }

        void setTransparency(boolean isMoving) {
            if (isMoving) {
                shadeView.setBackgroundColor(rootService.getColor(R.color.color_shade_transparent));
            } else {
                shadeView.setBackgroundColor(rootService.getColor(R.color.color_shade_normal));
            }
        }
    }
}
