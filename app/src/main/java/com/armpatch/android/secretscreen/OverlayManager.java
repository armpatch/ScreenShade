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

import androidx.core.view.MotionEventCompat;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class OverlayManager {

    Service parentService;
    WindowManager windowManager;
    WindowManager.LayoutParams dragBarParams;
    static int windowLayoutType;

    // Views
    LinearLayout uiDragBarLayout;
    ImageView uiDragBarView;

    private int displayHeight;
    private int displayWidth;

    float lastTouchY;
    int activePointerId;
    float posY;

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
        setDisplayHeight();
    }

    public void start() {
        addWindows();

    }

    public void stop() {
        removeWindows();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addWindows() {
        dragBarParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        dragBarParams.gravity = Gravity.BOTTOM;

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
                        final int pointerIndex =
                                event.findPointerIndex(activePointerId);

                        final float y = event.getRawY();

                        // Calculate the distance moved
                        final float dy = y - lastTouchY;

                        posY += dy;
                        dragBarParams.y = -1 * (int) posY;

                        windowManager.updateViewLayout(uiDragBarLayout, dragBarParams);

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

        windowManager.addView(uiDragBarLayout, dragBarParams);
    }

    private void removeWindows() {
        windowManager.removeView(uiDragBarLayout);
    }

    private void setDisplayHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;
    }

}
