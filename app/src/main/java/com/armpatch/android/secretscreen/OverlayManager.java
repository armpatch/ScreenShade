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
    private WindowManager.LayoutParams barParams;
    private WindowManager.LayoutParams blockerParams;
    static int windowLayoutType;

    // Views
    private LinearLayout barLayout;
    private ImageView barImageView;
    private LinearLayout blockerLayout;
    private ImageView blockerImageView;

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

        barParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        barParams.gravity = Gravity.TOP;
        barLayout = (LinearLayout) View.inflate(parentService, R.layout.ui_drag_bar, null);
        barImageView = barLayout.findViewById(R.id.ui_drag_bar);
        barImageView.getLayoutParams().width = displayWidth;

        barLayout.setOnTouchListener(new View.OnTouchListener() {
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
                        //activePointerId = INVALID_POINTER_ID;
                        break;
                    }
                }
                return true;
            }
        });

        blockerParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        blockerParams.gravity = Gravity.TOP;
        blockerLayout = (LinearLayout) View.inflate(parentService, R.layout.window_shade, null);
        blockerImageView = blockerLayout.findViewById(R.id.window_shade_image);
        blockerImageView.getLayoutParams().width = displayWidth;


        windowManager.addView(barLayout, barParams);
        windowManager.addView(blockerLayout, blockerParams);

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
        barParams.y = (int) dragBarPosY;

        blockerParams.y = barImageView.getHeight() + (int) dragBarPosY + 20;
        blockerImageView.getLayoutParams().height = displayHeight - blockerParams.y;

        windowManager.updateViewLayout(barLayout, barParams);
        windowManager.updateViewLayout(blockerLayout, blockerParams);
    }

    private void removeViews() {
        windowManager.removeView(barLayout);
        windowManager.removeView(blockerLayout);
    }

    private void setDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;
    }

}
