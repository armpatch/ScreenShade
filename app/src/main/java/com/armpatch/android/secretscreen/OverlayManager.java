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

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class OverlayManager {

    private Service parentService;
    private WindowManager windowManager;
    private WindowManager.LayoutParams dragBarParams;
    private WindowManager.LayoutParams windowShadeParams;
    static int windowLayoutType;

    // Views
    private LinearLayout uiDragBarLayout;
    private ImageView uiDragBarView;
    private LinearLayout uiWindowShadeLayout;
    private ImageView uiWindowShadeView;

    // phones display information
    private int displayHeight;
    private int displayWidth;

    float lastTouchY;
    int activePointerId;
    float dragBarPosY;

    // sets windowLayoutType
    static {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    public OverlayManager(Service service) {
        parentService = service;
        windowManager = (WindowManager) parentService.getSystemService(Service.WINDOW_SERVICE);
        setDisplayMetrics();
    }

    public void start() {
        createViews();
    }

    public void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createViews() {

        dragBarParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        dragBarParams.gravity = Gravity.TOP;
        uiDragBarLayout = (LinearLayout) View.inflate(parentService, R.layout.ui_drag_bar, null);
        uiDragBarView = uiDragBarLayout.findViewById(R.id.ui_drag_bar);
        uiDragBarView.getLayoutParams().width = displayWidth;

        uiDragBarLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        final int pointerIndex = event.getActionIndex();
                        lastTouchY = event.getRawY();

                        // Save the ID of this pointer (for dragging)
                        activePointerId = event.getPointerId(0);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        // Find the index of the active pointer and fetch its position
                        final int pointerIndex = event.findPointerIndex(activePointerId);

                        // Calculate the distance moved
                        final float y = event.getRawY();
                        final float dy = y - lastTouchY;

                        incrementDragBarPosY(dy);

                        // Remember this touch position for the next move event
                        lastTouchY = y;

                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        activePointerId = INVALID_POINTER_ID;
                        break;
                    }
                }
                return true;
            }
        });

        windowShadeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        windowShadeParams.gravity = Gravity.TOP;
        uiWindowShadeLayout = (LinearLayout) View.inflate(parentService, R.layout.window_shade, null);
        uiWindowShadeView = uiWindowShadeLayout.findViewById(R.id.window_shade_image);
        uiWindowShadeView.getLayoutParams().width = displayWidth;


        windowManager.addView(uiDragBarLayout, dragBarParams);
        windowManager.addView(uiWindowShadeLayout, windowShadeParams);

        setDragBarPosY(1300);
    }

    private void incrementDragBarPosY(float dy) {
        dragBarPosY += dy;
        updateViews();
    }

    private void setDragBarPosY(float y) {
        dragBarPosY = y;
        updateViews();
    }

    private void updateViews() {
        dragBarParams.y = (int) dragBarPosY;

        windowShadeParams.y = uiDragBarView.getHeight() + (int) dragBarPosY + 20;
        uiWindowShadeView.getLayoutParams().height = displayHeight - windowShadeParams.y;


        windowManager.updateViewLayout(uiDragBarLayout, dragBarParams);
        windowManager.updateViewLayout(uiWindowShadeLayout,  windowShadeParams);
    }

    private void removeViews() {
        windowManager.removeView(uiDragBarLayout);
        windowManager.removeView(uiWindowShadeLayout);
    }

    private void setDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;
    }

}
