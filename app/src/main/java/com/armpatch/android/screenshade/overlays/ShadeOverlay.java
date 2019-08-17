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
import com.armpatch.android.screenshade.overlays.animation.ShadeAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

@SuppressLint("ClickableViewAccessibility")
class ShadeOverlay {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;
    private DisplayInfo displayInfo;

    private View shadeFrame;
    private View shadeImageView;
    private Point viewCenterPoint;

    private ObjectAnimator revealAnimator;
    private ObjectAnimator hideAnimator;

    private WindowManager.LayoutParams layoutParams;

    private boolean isAddedToWindowManager;

    interface Callbacks {
        void onShadeRemoved();
    }

    ShadeOverlay(OverlayManager overlayManager) {
        this.service = overlayManager.service;
        windowManager = getWindowManager();
        callbacks = overlayManager;
        displayInfo = new DisplayInfo(service);

        inflateViews();
        setInitialLayoutParams();
        setShadeDimensions();
        setAnimators();
    }

    void revealFromPoint(Point centerPoint) {
        if (!revealAnimator.isRunning() && !hideAnimator.isRunning()) {
            this.viewCenterPoint = centerPoint;
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
        revealAnimator = ShadeAnimator.getRevealAnimatorSet(shadeImageView);


        hideAnimator = ShadeAnimator.getHideAnimator(shadeImageView);
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
                callbacks.onShadeRemoved();
            }
        });
    }

    private void inflateViews() {
        shadeFrame = View.inflate(service, R.layout.shade, null);
        shadeImageView = shadeFrame.findViewById(R.id.shade_circle);

        setOnTouchListener();
    }

    private void setOnTouchListener() {
        shadeFrame.setOnTouchListener(new View.OnTouchListener() {
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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

        layoutParams.width = displayInfo.getWidth();

        layoutParams.height = displayInfo.getHeight() + displayInfo.getNavBarHeight();
    }

    private void setShadeDimensions() {
        int circleDiameter = 2 * ( displayInfo.getDiagonal() + displayInfo.getNavBarHeight());

        shadeImageView.getLayoutParams().height = circleDiameter;
        shadeImageView.getLayoutParams().width = circleDiameter;
    }

    private void setShadeCirclePosition(Point origin) {
        Point offsetPoint = DisplayInfo.getCenterShiftedPoint(shadeImageView, origin);

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
