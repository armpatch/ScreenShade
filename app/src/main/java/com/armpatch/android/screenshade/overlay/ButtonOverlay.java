package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
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
        void onButtonDismissed();
    }

    ButtonOverlay(Callbacks callbacks, Context appContext) {
        super(appContext);

        this.callbacks = callbacks;

        windowManagerView = View.inflate(appContext, R.layout.button_overlay, null);
        windowManagerView.setLayoutParams(new FrameLayout.LayoutParams(0,0));
        trashZoneOverlay = new TrashZoneOverlay(appContext);

        setInitialLayoutParams();
        setTouchListener();
        getAnimators();
        updatePositionOnScreen(new Point(450, 1000)); // TODO needs to work for multiple screen sizes
    }

    @Override
    void updatePositionOnScreen(Point point) {
        super.updatePositionOnScreen(moveViewIntoScreenBounds(point));
    }

    void startRevealAnimation() {
        addViewToWindowManager();
        windowManagerView.setVisibility(View.VISIBLE);
        expandAnimator.start();
    }

    private void pressButton() {
        fadeAnimator.start();
        callbacks.onButtonTapped(getWindowCenterPoint());
    }

    void dismissButton() {
        cancelAllAnimators();

        shrinkAnimator.start();
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                windowManagerView.setVisibility(View.GONE);
                callbacks.onButtonDismissed();
            }
        });
    }

    private void cancelAllAnimators() {
        for (Animator animator :
                animatorList) {
            animator.cancel();
        }
    }

    private void setInitialLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();
    }

    private void getAnimators() {
        expandAnimator = ButtonAnimator.getRevealAnimator(windowManagerView);
        animatorList.add(expandAnimator);

        shrinkAnimator = ButtonAnimator.getHideAnimator(windowManagerView);
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });
        animatorList.add(shrinkAnimator);

        fadeAnimator = FadeAnimator.getFadeAnimator(windowManagerView);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                windowManagerView.setVisibility(View.INVISIBLE);
                removeViewFromWindowManager();
            }
        });
        animatorList.add(shrinkAnimator);
    }

    private void setTouchListener() {
        View frame = windowManagerView.findViewById(R.id.button_frame);

        frame.setOnTouchListener(new View.OnTouchListener() {

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
                        initialTouchPos.set((int) event.getX(), (int) event.getY());
                        initialPosition.set(layoutParams.x, layoutParams.y);
                        pressTime = System.currentTimeMillis();

                        trashZoneOverlay.show();

                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        dX = (int) event.getX() - initialTouchPos.x;
                        dY = (int) event.getY() - initialTouchPos.y;

                        Point currentPosition = new Point(initialPosition.x + dX, initialPosition.y + dY);
                        updatePositionOnScreen(currentPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        trashZoneOverlay.hide();

                        long timeSincePress = System.currentTimeMillis() - pressTime;

                        if (timeSincePress < 300 && !buttonMoved(dX, dY))
                            pressButton();

                        if (isInTrashZone( (int) event.getRawY() ) )
                            dismissButton();
                    }
                }
                return false;
            }
        });
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
        return  2 < Math.abs(dx) ||
                2 < Math.abs(dy);
    }

}
