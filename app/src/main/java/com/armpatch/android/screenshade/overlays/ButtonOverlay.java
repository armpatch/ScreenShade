package com.armpatch.android.screenshade.overlays;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.overlays.animation.ButtonAnimator;
import com.armpatch.android.screenshade.overlays.animation.DimmerAnimator;
import com.armpatch.android.screenshade.overlays.animation.FadeAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

@SuppressLint("ClickableViewAccessibility")
class ButtonOverlay {

    private Callbacks callbacks;
    private OverlayService service;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View buttonContainer;

    private Point savedPosition;

    private ObjectAnimator expandAnimator;
    private ObjectAnimator shrinkAnimator;
    private ObjectAnimator fadeAwayAnimator;

    private boolean isAddedToWindowManager;

    interface Callbacks {
        void onButtonClicked(Point currentPoint);
    }

    ButtonOverlay(OverlayManager overlayManager) {
        this.service = overlayManager.service;

        callbacks = overlayManager;
        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        inflateViews();
        setInitialLayoutParams();
        initAnimators();
    }

    void reveal() {
        if (!expandAnimator.isRunning() && !shrinkAnimator.isRunning()){
            if (savedPosition == null) setDefaultPosition();

            addViewToWindowManager();

            expandAnimator.start();
        }
    }

    void hide() {
        if (!isAddedToWindowManager)
            return;

        if (!expandAnimator.isRunning() && !shrinkAnimator.isRunning())
            shrinkAnimator.start();
    }

    private void inflateViews() {
        buttonContainer = View.inflate(service, R.layout.button, null);
        ImageButton imageButton = buttonContainer.findViewById(R.id.button);

        setOnTouchListener(imageButton);
    }

    private void setInitialLayoutParams() {
        View v = buttonContainer.findViewById(R.id.button_container_view);

        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        // a bigger window gives the imageButton extra space to expand into during the reveal
        // animation without the sides being cropped.
        float WINDOW_TO_BUTTON_RATIO = 1.2f;

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = (int) (width * WINDOW_TO_BUTTON_RATIO);
        layoutParams.height = (int) (height * WINDOW_TO_BUTTON_RATIO);
    }

    private void initAnimators() {
        expandAnimator = ButtonAnimator.getRevealAnimator(buttonContainer);

        shrinkAnimator = ButtonAnimator.getHideAnimator(buttonContainer);
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });

        fadeAwayAnimator = FadeAnimator.getFadeAwayAnimator(buttonContainer);
        fadeAwayAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });
    }

    private void setOnTouchListener(ImageButton button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            Point firstDown = new Point();
            Point location = new Point();
            Long startTime;

            int movementX;
            int movementY;

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
                        movementX = (int) event.getX() - firstDown.x;
                        movementY = (int) event.getY() - firstDown.y;

                        if (2 < Math.abs(movementX) && 2 < Math.abs(movementY))
                            makeButtonTransparent();

                        Point newPosition = new Point();
                        newPosition.set(
                                location.x + movementX,
                                location.y + movementY
                        );
                        updatePosition(newPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        makeButtonOpaque();
                        long currentTime = System.currentTimeMillis();
                        long timeSincePress = currentTime - startTime;
                        if (timeSincePress < 300 && Math.abs(movementX) < 2 && Math.abs(movementY) < 2) {
                            callbacks.onButtonClicked(getWindowCenterPoint());
                            fadeAwayAnimator.start();
                            //shrinkAnimator.start();
                        }
                        Point upPoint = new Point((int)event.getRawX(), (int)event.getRawY());
                        if (pointIsInTrashZone(upPoint)) {
                            hide();
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

    private void setDefaultPosition() {
        savedPosition = new Point(
                450,
                800);

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
            isAddedToWindowManager = true;
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        try {
            windowManager.removeView(buttonContainer);
            isAddedToWindowManager = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Point getWindowCenterPoint() {
        Point currentPoint = new Point(layoutParams.x, layoutParams.y);

        currentPoint.offset(layoutParams.width / 2, layoutParams.height / 2);

        return currentPoint;
    }

    private void makeButtonTransparent() {
        float minAlpha = 0.5f;
        buttonContainer.setAlpha(minAlpha);
    }

    private void makeButtonOpaque() {
        DimmerAnimator.getAnimator(buttonContainer,
                buttonContainer.getAlpha(), 1f).start();
    }

    private boolean pointIsInTrashZone(Point point) {
        int Y_MIN = Display.getHeight(service) - 400;

        boolean result = Y_MIN < point.y;

        Log.i("coordinate", "--- Is in Zone --- " + result);
        return result;
    } // TODO method refactor out of class



}
