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

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.overlays.animation.ShadeAnimatorFactory;
import com.armpatch.android.screenshade.services.OverlayService;

@SuppressLint("ClickableViewAccessibility")
class ShadeOverlay {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;

    private View shadeFrame;
    private View shadeImageView;
    private Point viewCenterPoint;

    private ObjectAnimator revealAnimator;
    private ObjectAnimator hideAnimator;

    private WindowManager.LayoutParams layoutParams;

    private boolean isAddedToWindowManager;

    interface Callbacks {
        void onShadeRemoved(Point AnimationEndpoint);
    }

    ShadeOverlay(OverlayManager overlayManager) {
        this.service = overlayManager.service;
        windowManager = getWindowManager();

        callbacks = overlayManager;

        inflateViews();
        setInitialLayoutParams();
        calculateExpandedCircleDimensions();
        setAnimators();
    }

    void revealFromPoint(Point centerPoint) {
        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            this.viewCenterPoint = centerPoint;
            addViewToWindowManager();
            setImageViewXYFrom(centerPoint);

            revealAnimator.start();
        }
    }

    void hide() {
        if (!isAddedToWindowManager)
            return;

        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            setImageViewXYFrom(viewCenterPoint);

            hideAnimator.start();
        }
    }

    private void setAnimators() {
        revealAnimator = (ObjectAnimator) ShadeAnimatorFactory.getRevealAnimator(shadeImageView);

        hideAnimator = (ObjectAnimator) ShadeAnimatorFactory.getHideAnimator(shadeImageView);
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();

            }
        });
    }

    private void inflateViews() {
        shadeFrame = View.inflate(service, R.layout.floating_shade, null);
        shadeImageView = shadeFrame.findViewById(R.id.shade_circle);

        setOnTouchListener(shadeFrame);
    }

    private void setOnTouchListener(View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            int DOUBLE_TAP_DURATION = 300;
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

        layoutParams.height = Display.getHeight(service) + Display.getNavBarHeight(service);
    }

    private void calculateExpandedCircleDimensions() {
        int diameter = 2 * ( Display.getDiagonal(service) + Display.getNavBarHeight(service));

        shadeImageView.getLayoutParams().height = diameter;
        shadeImageView.getLayoutParams().width = diameter;
    }

    private void setImageViewXYFrom(Point origin) {
        Point offsetPoint = CoordinateMaker.getCenterShiftedPoint(shadeImageView, origin);

        shadeImageView.setX(offsetPoint.x);
        shadeImageView.setY(offsetPoint.y);
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(shadeFrame, layoutParams);
            isAddedToWindowManager = true;
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        try {
            windowManager.removeView(shadeFrame);
            isAddedToWindowManager = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WindowManager getWindowManager() {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
