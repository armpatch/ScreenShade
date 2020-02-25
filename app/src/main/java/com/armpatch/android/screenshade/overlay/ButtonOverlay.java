package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ButtonAnimator;
import com.armpatch.android.screenshade.animation.FadeAnimator;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
class ButtonOverlay extends Overlay{

    private TrashZoneOverlay trashZoneOverlay;
    private Callbacks callbacks;

    private ArrayList<Animator> animatorList = new ArrayList<>();
    private ObjectAnimator expandAnimator;
    private ObjectAnimator shrinkAnimator;
    private ObjectAnimator fadeAnimator;

    interface Callbacks {
        void onButtonTapped(Point centerOfButton);
        void onButtonHidden();
    }

    ButtonOverlay(Callbacks callbacks, Context appContext) {
        super(appContext);

        this.callbacks = callbacks;

        windowManagerView = View.inflate(appContext, R.layout.button_overlay, null);
        windowManagerView.setLayoutParams(new FrameLayout.LayoutParams(0,0));
        trashZoneOverlay = new TrashZoneOverlay(appContext);

        setInitialLayoutParams();
        setTouchListener();
        setupAnimators();

        setPositionOnScreen(new Point(450, 1000)); // TODO needs to work for multiple screen sizes
    }

    void startRevealAnimation() {
        addViewToWindowManager();
        windowManagerView.setVisibility(View.VISIBLE);
        expandAnimator.start();
    }

    private void setInitialLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();
    }

    private void setupAnimators() {
        expandAnimator = ButtonAnimator.getRevealAnimator(windowManagerView);

        shrinkAnimator = ButtonAnimator.getHideAnimator(windowManagerView);
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });

        fadeAnimator = FadeAnimator.getFadeAnimator(windowManagerView);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                windowManagerView.setVisibility(View.INVISIBLE);
                removeViewFromWindowManager();
            }
        });

        animatorList.add(expandAnimator);
        animatorList.add(shrinkAnimator);
        animatorList.add(fadeAnimator);
    }

    private void setTouchListener() {
        final View frame = windowManagerView.findViewById(R.id.button_frame);

        frame.setOnTouchListener(new View.OnTouchListener() {

            VelocityTracker velocityTracker;
            Point initialTouchPos = new Point();
            Point initialPosition = new Point();
            Long pressTime;

            int dX;
            int dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                event.setLocation(event.getRawX(), event.getRawY());

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN: {
                        velocityTracker = VelocityTracker.obtain();
                        initialTouchPos.set((int) event.getX(), (int) event.getY());
                        initialPosition.set(layoutParams.x, layoutParams.y);
                        pressTime = System.currentTimeMillis();
                        //frame.setAlpha(0.8f);

                        trashZoneOverlay.show();

                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        dX = (int) event.getX() - initialTouchPos.x;
                        dY = (int) event.getY() - initialTouchPos.y;
                        velocityTracker.addMovement(event);

                        Point currentPosition = new Point(initialPosition.x + dX, initialPosition.y + dY);
                        setPositionOnScreen(currentPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {

                        trashZoneOverlay.hide();
                        //frame.setAlpha(1.0f);


                        velocityTracker.computeCurrentVelocity(1);
                        float xVelocity = velocityTracker.getXVelocity();
                        float yVelocity = velocityTracker.getYVelocity();

                        if (Math.abs(xVelocity) > .5 || Math.abs(yVelocity) > .5)
                            //startInertiaAnimation(xVelocity, yVelocity);

                        velocityTracker.recycle();

                        long timeSincePress = System.currentTimeMillis() - pressTime;

                        if (timeSincePress < 300 && !buttonMoved(dX, dY)) {
                            pressButton();
                            break;
                        }
                        if (isInTrashZone( (int) event.getRawY() ))
                            hide();

                        break;
                    }
                }
                return false;
            }
        });
    }

    private void pressButton() {
        fadeAnimator.start();
        callbacks.onButtonTapped(getWindowCenterPoint());
    }

    void hide() {
        cancelAllAnimators();

        shrinkAnimator.start();
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                windowManagerView.setVisibility(View.GONE);
                callbacks.onButtonHidden();
            }
        });
    }

    private void cancelAllAnimators() {
        for (Animator animator :
                animatorList) {
            animator.cancel();
        }
    }

    private Point getWindowCenterPoint() {
        Point currentPoint = new Point(layoutParams.x, layoutParams.y);

        currentPoint.offset(windowManagerView.getWidth() / 2, windowManagerView.getWidth() / 2);

        return currentPoint;
    }

    private boolean isInTrashZone(int positionY) {
        int ZONE_HEIGHT = 200;
        return displayInfo.getScreenHeight() - ZONE_HEIGHT < positionY;
    }

    private boolean buttonMoved(int dx, int dy) { // TODO needs better name
        return  4 < Math.abs(dx) ||
                4 < Math.abs(dy);
    }

    @Override
    void setPositionOnScreen(Point point) {
        super.setPositionOnScreen(moveViewIntoScreenBounds(point));
    }

    private Point moveViewIntoScreenBounds(Point point) {
        int Y_MAX = displayInfo.getScreenHeight() - windowManagerView.getHeight();
        int X_MAX = displayInfo.getScreenWidth() - windowManagerView.getWidth();

        // Adjust x
        if (point.x < 0) point.x = 0;
        if (point.x > X_MAX) point.x = X_MAX;

        // Adjust Y
        if (point.y < 0) point.y = 0;
        if (point.y > Y_MAX) point.y = Y_MAX;

        return point;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void flipXY() {
        displayInfo.update();

        int temp = layoutParams.y;
        layoutParams.y = layoutParams.x;
        layoutParams.x = temp;

        if (isAddedToWindowManager) {
            removeViewFromWindowManager();
            addViewToWindowManager();
        }

    }
}
