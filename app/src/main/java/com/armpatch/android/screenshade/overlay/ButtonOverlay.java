package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ButtonAnimator;
import com.armpatch.android.screenshade.animation.DimmerAnimator;
import com.armpatch.android.screenshade.animation.FadeAnimator;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
class ButtonOverlay extends Overlay{

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

        windowManagerView = View.inflate(appContext, R.layout.button, null);
        windowManagerView.setLayoutParams(new FrameLayout.LayoutParams(0,0));

        setInitialLayoutParams();
        setupButtonWithTouchListener();
        setupAnimators();
        updatePositionOnScreen(new Point(450, 800)); // TODO needs to work for multiple screen sizes
    }

    @Override
    void updatePositionOnScreen(Point point) {
        super.updatePositionOnScreen(getPointWithinScreenBounds(point));
    }

    void startRevealAnimation() {
        addViewToWindowManager();

        expandAnimator.start();
    }

    private void hideButtonAndShowShade() {
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

        View buttonOutline = windowManagerView.findViewById(R.id.button_outline);

        // a bigger window gives the imageButton extra space to expand into during the reveal
        // animation without the sides being cropped.
        float WINDOW_TO_BUTTON_RATIO = 1.2f;

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = (int) (buttonOutline.getLayoutParams().width * WINDOW_TO_BUTTON_RATIO);
        layoutParams.height = (int) (buttonOutline.getLayoutParams().height * WINDOW_TO_BUTTON_RATIO);
    }

    private void setupAnimators() {
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
                removeViewFromWindowManager();
            }
        });
        animatorList.add(shrinkAnimator);
    }

    private void setupButtonWithTouchListener() {
        ImageButton button = windowManagerView.findViewById(R.id.button);
        button.setOnTouchListener(new View.OnTouchListener() {

            Point firstDown = new Point();
            Point buttonStart = new Point();
            Long startTime;

            int dX;
            int dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                event.setLocation(event.getRawX(), event.getRawY()); //sets the absolute location not relative to views

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        firstDown.set((int) event.getX(), (int) event.getY());
                        buttonStart.set(layoutParams.x, layoutParams.y);
                        startTime = System.currentTimeMillis();
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        dX = (int) event.getX() - firstDown.x;
                        dY = (int) event.getY() - firstDown.y;

                        if (isOverThreshold(dX, dY)) windowManagerView.setAlpha(0.5f);

                        Point newButtonPosition = new Point(buttonStart.x + dX,buttonStart.y + dY);
                        updatePositionOnScreen(newButtonPosition);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        animateTransparencyToNormal();

                        long timeSincePress = System.currentTimeMillis() - startTime;

                        if (timeSincePress < 300 && !isOverThreshold(dX, dY)) {
                            hideButtonAndShowShade();
                        }

                        if (isInTrashZone((int) event.getRawX())) dismissButton();
                    }
                }
                return false;
            }
        });
    }

    private void animateTransparencyToNormal() {
        DimmerAnimator.getAnimator(windowManagerView, windowManagerView.getAlpha(), 1f).start();
    }

    private Point getPointWithinScreenBounds(Point original) {
        Point result = new Point(original);

        int Y_MAX = displayInfo.getHeight() - windowManagerView.getLayoutParams().height;
        int X_MAX = displayInfo.getWidth() - windowManagerView.getLayoutParams().width;

        if (X_MAX < result.x) result.x = X_MAX;
        if (Y_MAX < result.y) result.y = Y_MAX;

        return result;
    }

    private Point getWindowCenterPoint() {
        Point currentPoint = new Point(layoutParams.x, layoutParams.y);

        currentPoint.offset(layoutParams.width / 2, layoutParams.height / 2);

        return currentPoint;
    }

    private boolean isInTrashZone(int positionY) {
        int ZONE_HEIGHT = 200;
        return displayInfo.getHeight() - ZONE_HEIGHT < positionY;
    }

    private boolean isOverThreshold(int dx, int dy) { // TODO needs better name
        return  2 < Math.abs(dx) ||
                2 < Math.abs(dy);
    }

    private void logIsNull(@Nullable Object object) {
        String messageStart = "Null Test :  ";
        String messageEnd;

        if (object == null) {
            messageEnd = "----------- NULL -----------";
        } else {
            messageEnd = object.toString();
        }

        Log.i("logThis", messageStart + messageEnd);
    }

}
