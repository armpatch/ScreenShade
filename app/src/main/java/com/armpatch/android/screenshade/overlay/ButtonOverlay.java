package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ButtonAnimator;
import com.armpatch.android.screenshade.animation.DimmerAnimator;
import com.armpatch.android.screenshade.animation.FadeAnimator;

@SuppressLint("ClickableViewAccessibility")
class ButtonOverlay extends Overlay{

    private Callbacks callbacks;

    private ObjectAnimator expandAnimator;
    private ObjectAnimator shrinkAnimator;
    private ObjectAnimator fadeAwayAnimator;

    interface Callbacks {
        void onButtonClicked(Point center);
        void onButtonTrashed();
    }

    ButtonOverlay(Callbacks callbacks, Context appContext) {
        super(appContext);

        this.callbacks = callbacks;

        windowManagerView = View.inflate(appContext, R.layout.button, null);

        setInitialLayoutParams();
        setupButton();
        setupAnimators();
        updatePosition(new Point(450, 800)); // TODO clean up
    }

    void reveal() {
        if (!expandAnimator.isRunning() && !shrinkAnimator.isRunning()){

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

    void setInitialLayoutParams() {
        View v = windowManagerView.findViewById(R.id.button_container_view);

        // a bigger window gives the imageButton extra space to expand into during the reveal
        // animation without the sides being cropped.
        float WINDOW_TO_BUTTON_RATIO = 1.2f;

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = (int) (v.getLayoutParams().width * WINDOW_TO_BUTTON_RATIO);
        layoutParams.height = (int) (v.getLayoutParams().height * WINDOW_TO_BUTTON_RATIO);
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

        fadeAwayAnimator = FadeAnimator.getFadeAwayAnimator(windowManagerView);
        fadeAwayAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });
    }

    private void setupButton() {
        ImageButton button = windowManagerView.findViewById(R.id.button);
        button.setOnTouchListener(new View.OnTouchListener() {

            Point firstDown = new Point();
            Point location = new Point();
            Long startTime;

            int dX;
            int dY;

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
                        dX = (int) event.getX() - firstDown.x;
                        dY = (int) event.getY() - firstDown.y;

                        if (isOverThreshold(dX, dY)) windowManagerView.setAlpha(0.5f);

                        Point newPosition = new Point();
                        newPosition.set(
                                location.x + dX,
                                location.y + dY
                        );
                        updatePosition(newPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        animateToOpaque();
                        long currentTime = System.currentTimeMillis();
                        long timeSincePress = currentTime - startTime;
                        if (timeSincePress < 300 && isUnderThreshold(dX, dY)) {
                            callbacks.onButtonClicked(getWindowCenterPoint());
                            fadeAwayAnimator.start();
                            //shrinkAnimator.start();
                        }
                        Point upPoint = new Point((int)event.getRawX(), (int)event.getRawY());
                        if (isInTrashZone(upPoint.y)) {
                            hide();
                            callbacks.onButtonTrashed();
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

    @Override
    void updatePosition(Point point) {
        movePointIntoScreenBounds(point);
        super.updatePosition(point);
    }

    private void movePointIntoScreenBounds(Point originalPoint) {
        int Y_MAX = displayInfo.getHeight() - windowManagerView.getLayoutParams().height;
        int X_MAX = displayInfo.getWidth() - windowManagerView.getLayoutParams().width;

        if (X_MAX < originalPoint.x) originalPoint.x = X_MAX;

        if (Y_MAX < originalPoint.y) originalPoint.y = Y_MAX;
    }

    private Point getWindowCenterPoint() {
        Point currentPoint = new Point(layoutParams.x, layoutParams.y);

        currentPoint.offset(layoutParams.width / 2, layoutParams.height / 2);

        return currentPoint;
    }

    private void animateToOpaque() {
        DimmerAnimator.getAnimator(windowManagerView,
                windowManagerView.getAlpha(), 1f).start();
    }

    private boolean isInTrashZone(int positionY) {
        int ZONE_HEIGHT = 200;
        return displayInfo.getHeight() - ZONE_HEIGHT < positionY;
    }

    private boolean isOverThreshold(int dx, int dy) { // TODO needs better name
        return 2 < Math.abs(dx) &&
                2 < Math.abs(dy);
    }

    private boolean isUnderThreshold(int dx, int dy) { // TODO needs better name
        return Math.abs(dx) < 2 &&
                Math.abs(dy) < 2;
    }
}
