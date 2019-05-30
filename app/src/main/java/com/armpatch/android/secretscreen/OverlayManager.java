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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class OverlayManager {

    private Service parentService;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    static int windowLayoutType;

    // Views
    private LinearLayout comboLayout;
    private ImageView barImageView;
    private ImageView shadeImageView;
    private View testButtonView;
    private Button testButton;
    private Button testButton2;

    // phones display information
    private int displayHeight;
    private int displayWidth;

    float lastTouchY;
    float dragBarPosY;

    //test variables
    final int shadeHeight1 = 200;
    final int shadeHeight2 = 1000;
    private boolean shadeIsUp;


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
        addViews();
    }

    public void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {

        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        layoutParams.gravity = Gravity.TOP;
        comboLayout = (LinearLayout) View.inflate(parentService, R.layout.ui_combo_layout, null);
        barImageView = comboLayout.findViewById(R.id.drag_bar_bottom);
        //barImageView.getLayoutParams().width = displayWidth;

        barImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouchY = event.getRawY();
                        isDragging(true);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        final float y = event.getRawY();
                        final float dy = y - lastTouchY;
                        incrementDragBarPosY(dy);

                        // Remember this touch position for the next move event
                        lastTouchY = y;
                        break;

                    case MotionEvent.ACTION_UP:
                        isDragging(false);
                        break;
                }
                return true;
            }
        });

        shadeImageView = comboLayout.findViewById(R.id.shade_bottom);
        //TODO remove after testing
        shadeImageView.getLayoutParams().height = 200;

        testButtonView = View.inflate(parentService, R.layout.test_button, null);
        testButton = (Button) testButtonView.findViewById(R.id.test_button);
        testButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        shadeIsUp = true;
                        shadeImageView.getLayoutParams().height = 300;
                        updateWindows();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        shadeIsUp = false;
                        shadeImageView.getLayoutParams().height = 1000;
                        updateWindows();
                        break;
                }
                return false;
            }
        });

        testButton2 = (Button) testButtonView.findViewById(R.id.test_button_2);
        testButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        setDragBarPosY(1500);
                        updateWindows();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        setDragBarPosY(400);
                        updateWindows();
                        break;
                }
                return false;
            }
        });

        WindowManager.LayoutParams testButtonParams = layoutParams;
        testButtonParams.gravity = Gravity.TOP;
        testButtonParams.y = 100;
        windowManager.addView(testButtonView, testButtonParams);

        windowManager.addView(comboLayout, layoutParams);
        setDragBarPosY(600);

        updateWindows();
    }

    private void incrementDragBarPosY(float dy) {
        dragBarPosY += dy;
        updateWindows();
    }

    private void setDragBarPosY(float y) {
        dragBarPosY = y;
        updateWindows();
    }

    private void updateWindows() {
        layoutParams.y = (int) dragBarPosY;

        shadeImageView.getLayoutParams().height = displayHeight - layoutParams.y;


        windowManager.updateViewLayout(comboLayout, layoutParams);
    }

    void isDragging(boolean dragging) {
        if (dragging) {
            shadeImageView.setBackgroundColor(parentService.getColor(R.color.color_shade_transparent));
        } else {
            shadeImageView.setBackgroundColor(parentService.getColor(R.color.color_shade_normal));
        }
    }

    private void removeViews() {
        windowManager.removeView(comboLayout);
        windowManager.removeView(testButtonView);
    }

    private void setDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;
    }

}
