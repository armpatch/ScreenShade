package com.armpatch.android.screenshade.overlays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ButtonRevealAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

class MovableButton {

    private OverlayService service;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View buttonLayout;
    private ImageButton button;

    private Point savedPosition;

    private Callbacks callbacks;

    interface Callbacks {
        void onButtonClicked();
    }

    MovableButton (OverlayManager overlayManager) {
        this.service = overlayManager.service;

        callbacks = (Callbacks) overlayManager;

        windowManager = getWindowManager(service);

        inflateViews();
        setLayoutParams();
    }

    private void setLayoutParams() {
        View v = buttonLayout.findViewById(R.id.button_container_view);

        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        float view_margin = 1.2f;

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = (int) (width * view_margin);
        layoutParams.height = (int) (height * view_margin);
    }

    void reveal() {
        if (savedPosition == null) setPositionToDefault();

        addViewToWindowManager();
        startRevealAnimation();
    }

    void hide() {
        removeViewFromWindowManager();

    }

    private void updatePosition(Point point) {
        adjustPointToScreenBoundaries(point);

        layoutParams.x = point.x;
        layoutParams.y = point.y;

        windowManager.updateViewLayout(buttonLayout, layoutParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inflateViews() {
        buttonLayout = View.inflate(service, R.layout.movable_button, null);
        button = buttonLayout.findViewById(R.id.button);

        addTouchListenerToButton();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTouchListenerToButton() {
        button.setOnTouchListener(new View.OnTouchListener() {

            Point firstDown = new Point();
            Point location = new Point();
            Long startTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //VelocityTracker tracker = VelocityTracker.obtain();

                event.setLocation(event.getRawX(), event.getRawY());
                //tracker.addMovement(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        firstDown.set((int) event.getX(), (int) event.getY());
                        location.set(layoutParams.x, layoutParams.y);
                        startTime = System.currentTimeMillis();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        int movementX = (int) event.getX() - firstDown.x;
                        int movementY = (int) event.getY() - firstDown.y;

                        Point newPosition = new Point();
                        newPosition.set(
                                location.x + movementX,
                                location.y + movementY
                        );
                        updatePosition(newPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        long currentTime = System.currentTimeMillis(); // TODO
                        long elapsedTime = currentTime - startTime;
                        if (elapsedTime < 300) {
                            callbacks.onButtonClicked();
                        }

                        //tracker.computeCurrentVelocity(1000);
                        //float velocityX = tracker.getXVelocity();
                        //float velocityY = tracker.getYVelocity();
                    }
                    //tracker.recycle();
                }
                return false;
            }
        });
    }

    private void startRevealAnimation() {
        ButtonRevealAnimator.get(buttonLayout).start();
    }

    private void setPositionToDefault() {
        savedPosition = new Point(100,1000); // arbitrary location for testing

        layoutParams.x = savedPosition.x;
        layoutParams.y = savedPosition.y;
    }

    private void adjustPointToScreenBoundaries(Point point) {
        int MARGIN_TOP = 0;
        int MARGIN_BOTTOM = 0;


        int Y_MIN = MARGIN_TOP;
        int Y_MAX = DisplayInfo.getDisplayHeight(service) - MARGIN_BOTTOM - getHeight();
        int X_MIN = 0;
        int X_MAX = DisplayInfo.getDisplayWidth(service) - getWidth();

        if (point.x < X_MIN)
            point.x = X_MIN;

        if (X_MAX < point.x)
            point.x = X_MAX;

        if (point.y < Y_MIN)
            point.y = Y_MIN;

        if (Y_MAX < point.y)
            point.y = Y_MAX;

    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(buttonLayout, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        windowManager.removeView(buttonLayout);
    }

    private int getHeight() {
        return buttonLayout.getLayoutParams().height;
    }

    private int getWidth() {
        return buttonLayout.getLayoutParams().width;
    }

    private WindowManager getWindowManager(OverlayService service) {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
