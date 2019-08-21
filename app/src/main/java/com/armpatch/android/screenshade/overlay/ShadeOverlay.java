package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ShadeAnimator;

@SuppressLint("ClickableViewAccessibility")
class ShadeOverlay extends Overlay{

    private Callbacks callbacks;

    private View shadeCircle;
    private Point viewCenterPoint;

    private ObjectAnimator revealAnimator;
    private ObjectAnimator hideAnimator;

    interface Callbacks{
        void onShadeRemoved();
    }

    ShadeOverlay(Callbacks callbacks, Context appContext) {
        super(appContext);

        this.callbacks = callbacks;

        windowManagerView = View.inflate(appContext, R.layout.shade_overlay, null);
        shadeCircle = windowManagerView.findViewById(R.id.shade_circle);

        setOnTouchListener();
        setInitialLayoutParams();
        setShadeDimensions();
        setAnimators();
    }

    void startRevealAnimationFromPoint(Point centerPoint) {
        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            viewCenterPoint = centerPoint;
            addViewToWindowManager();
            setShadeCirclePosition(centerPoint);

            revealAnimator.start();
        }
    }

    void hide() {
        if (!isAddedToWindowManager)
            return;

        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            setShadeCirclePosition(viewCenterPoint);

            hideAnimator.start();
        }
    }

    private void setAnimators() {
        revealAnimator = ShadeAnimator.getRevealAnimatorSet(shadeCircle);

        hideAnimator = ShadeAnimator.getHideAnimator(shadeCircle);
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
                callbacks.onShadeRemoved();
            }
        });
    }

    private void setOnTouchListener() {
        windowManagerView.setOnTouchListener(new View.OnTouchListener() {
            int DOUBLE_TAP_DURATION = 200;
            long duration;
            long lastTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                event.setLocation(event.getRawX(), event.getRawY());

                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    duration = System.currentTimeMillis() - lastTime;
                    if (duration < DOUBLE_TAP_DURATION) {
                        hide();
                    }
                    lastTime = System.currentTimeMillis();
                }
                return false;
            }
        });
    }

    private void setInitialLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        layoutParams.y = -displayInfo.getStatusBarHeight();
        layoutParams.width = displayInfo.getScreenWidth();
        layoutParams.height = displayInfo.getScreenHeight() + displayInfo.getNavBarHeight() +
                displayInfo.getStatusBarHeight();
    }

    private void setShadeDimensions() {
        int circleDiameter = 2 * ( displayInfo.getScreenDiagonal() + displayInfo.getNavBarHeight());

        shadeCircle.getLayoutParams().height = circleDiameter;
        shadeCircle.getLayoutParams().width = circleDiameter;
    }

    private void setShadeCirclePosition(Point origin) {
        Point offsetTopLeftPoint = DisplayInfo.getCenterShiftedPoint(shadeCircle, origin);
        offsetTopLeftPoint.offset(0, displayInfo.getStatusBarHeight());

        shadeCircle.setX(offsetTopLeftPoint.x);
        shadeCircle.setY(offsetTopLeftPoint.y);
    }


}
