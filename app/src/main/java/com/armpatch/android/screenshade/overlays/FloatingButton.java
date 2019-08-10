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
import com.armpatch.android.screenshade.animation.ButtonAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

@SuppressLint("ClickableViewAccessibility")
class FloatingButton {

    private Callbacks callbacks;
    private OverlayService service;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View buttonContainer;
    private Point savedPosition;

    interface Callbacks {
        void onButtonClicked(Point currentPoint);
    }

    FloatingButton(OverlayManager overlayManager) {
        this.service = overlayManager.service;

        callbacks = overlayManager;
        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        inflateViews();
        setFloatingWindowDimensions();
    }

    void reveal() {
        if (savedPosition == null) setPositionToDefault();

        addViewToWindowManager();
        ButtonAnimator.get(buttonContainer).start();
    }

    void hide() {
        removeViewFromWindowManager();
    }

    private void setFloatingWindowDimensions() {
        View v = buttonContainer.findViewById(R.id.button_container_view);

        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        // a bigger window gives the button extra space to expand into during the reveal
        // animation without the sides being cropped.
        float WINDOW_TO_BUTTON_RATIO = 1.2f;

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = (int) (width * WINDOW_TO_BUTTON_RATIO);
        layoutParams.height = (int) (height * WINDOW_TO_BUTTON_RATIO);
    }

    private void inflateViews() {
        buttonContainer = View.inflate(service, R.layout.floating_button, null);
        ImageButton button = buttonContainer.findViewById(R.id.button);

        setOnTouchListener(button);
    }

    private void setOnTouchListener(ImageButton button) {
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
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - startTime;
                        if (elapsedTime < 300) {
                            Log.i("FloatingButton.TAG", "FloatingButton.getWindowCenterPoint() = "
                                    + getWindowCenterPoint().toString());
                            callbacks.onButtonClicked(getWindowCenterPoint());
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

    private void updatePosition(Point point) {
        movePointIntoScreenBounds(point);

        layoutParams.x = point.x;
        layoutParams.y = point.y;

        windowManager.updateViewLayout(buttonContainer, layoutParams);
    }

    private void setPositionToDefault() {
        savedPosition = new Point(300,1200); // arbitrary starting location

        layoutParams.x = savedPosition.x;
        layoutParams.y = savedPosition.y;
    }

    private void movePointIntoScreenBounds(Point originalPoint) {
        int Y_MIN = 0;
        int Y_MAX = Display.getHeight(service) - buttonContainer.getLayoutParams().height;
        int X_MIN = 0;
        int X_MAX = Display.getWidth(service) - buttonContainer.getLayoutParams().width;

        if (originalPoint.x < X_MIN)
            originalPoint.x = X_MIN;

        if (X_MAX < originalPoint.x)
            originalPoint.x = X_MAX;

        if (originalPoint.y < Y_MIN)
            originalPoint.y = Y_MIN;

        if (Y_MAX < originalPoint.y)
            originalPoint.y = Y_MAX;
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(buttonContainer, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        windowManager.removeView(buttonContainer);
    }

    private Point getWindowCenterPoint() {
        Point currentPoint = new Point(layoutParams.x, layoutParams.y);

        currentPoint.offset(layoutParams.width / 2, layoutParams.height / 2);

        return currentPoint;
    }

}
