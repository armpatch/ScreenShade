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
import com.armpatch.android.screenshade.animation.DimmerAnimator;
import com.armpatch.android.screenshade.animation.ShadeAnimator;

@SuppressLint("ClickableViewAccessibility")
class ShadeOverlay extends Overlay {

    private Callbacks callbacks;

    private View shadeCircle;
    private Point viewCenterPoint;

    private ObjectAnimator revealAnimator;
    private ObjectAnimator hideAnimator;
    private ObjectAnimator dimmerAnimator;

    interface Callbacks {
        void onShadeRemoved();
    }

    ShadeOverlay(final Callbacks callbacks, Context appContext) {
        super(appContext);

        this.callbacks = callbacks;

        windowManagerView = new OverlayView(appContext, R.layout.shade_overlay, null) {
            @Override
            void onBackPressed() {
                hide();
            }
        };

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

        dimmerAnimator = DimmerAnimator.getAnimator(windowManagerView, 1.0f, 0.7f);
    }

    private void setOnTouchListener() {
        windowManagerView.setOnTouchListener(new View.OnTouchListener() {
            int DOUBLE_TAP_DURATION = 300;
            long duration;
            long lastTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                event.setLocation(event.getRawX(), event.getRawY());

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        duration = System.currentTimeMillis() - lastTime;
                        if (duration < DOUBLE_TAP_DURATION) {
                            hide();
                        } else {
                            lastTime = System.currentTimeMillis();
                            dimmerAnimator.start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        windowManagerView.setAlpha(1.0f);
                        dimmerAnimator.cancel();
                        break;
                }
                return false;
            }
        });
    }

    void hide() {
        if (!isAddedToWindowManager)
            return;

        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            setShadeCirclePosition(viewCenterPoint);

            hideAnimator.start();
        }
    }

    private void setInitialLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        layoutParams.y = -displayInfo.getStatusBarHeight();
        layoutParams.width = displayInfo.getScreenWidth();
        layoutParams.height = displayInfo.getScreenHeight() + displayInfo.getNavBarHeight() +
                displayInfo.getStatusBarHeight();
    }

    private void setShadeDimensions() {
        int circleDiameter = 2 * (displayInfo.getScreenDiagonal() + displayInfo.getNavBarHeight());

        shadeCircle.getLayoutParams().height = circleDiameter;
        shadeCircle.getLayoutParams().width = circleDiameter;
    }

    private void setShadeCirclePosition(Point origin) {
        Point offsetTopLeftPoint = DisplayInfo.getCenterShiftedPoint(shadeCircle, origin);
        offsetTopLeftPoint.offset(0, displayInfo.getStatusBarHeight());

        shadeCircle.setX(offsetTopLeftPoint.x);
        shadeCircle.setY(offsetTopLeftPoint.y);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void flipHeightAndWidth() {
        int temp = layoutParams.width;
        layoutParams.width = layoutParams.height;
        layoutParams.height = temp;
        if (isAddedToWindowManager) {
            removeViewFromWindowManager();
            addViewToWindowManager();
        }
    }

}
