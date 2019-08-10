package com.armpatch.android.screenshade.overlays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.CircularRevealAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

class CircularShade {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;

    private View shadeWindowView;
    private View circleImageView;

    private WindowManager.LayoutParams layoutParams;

    interface Callbacks {
        void onShadeRemoved();
    }

    @SuppressLint("ClickableViewAccessibility")
    CircularShade(OverlayManager overlayManager) {
        this.service = overlayManager.service;
        windowManager = getWindowManager();

        callbacks = overlayManager;

        inflateViews();
        setLayoutParams();
        setOverlayFinalDimensions();
    }

    void revealFromPoint(Point centerPoint) {
        addViewToWindowManager();
        setAnimationOrigin(centerPoint);
        startRevealAnimation();
    }

    void hideToPoint() {
        startHideAnimation();
    }

    private void inflateViews() {
        shadeWindowView = View.inflate(service, R.layout.overlay_shade, null);
        circleImageView = shadeWindowView.findViewById(R.id.shade_circle);
    }

    private void setLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();

        layoutParams.height = Display.getHeight(service) +
                Display.getNavBarHeight(service);
    }

    private void setOverlayFinalDimensions() {
        int diameter = 2 * ( Display.getDiagonal(service) + Display.getNavBarHeight(service));

        circleImageView.getLayoutParams().height = diameter;
        circleImageView.getLayoutParams().width = diameter;
    }

    private void setAnimationOrigin(Point origin) {
        Point offsetPoint = CoordinateMaker.getCenterShiftedPoint(circleImageView, origin);

        circleImageView.setX(offsetPoint.x);
        circleImageView.setY(offsetPoint.y);
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(shadeWindowView, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void startRevealAnimation() {
        CircularRevealAnimator.getRevealAnimator(circleImageView).start();
    }

    private void startHideAnimation() {
        CircularRevealAnimator.getHideAnimator(circleImageView).start();
    }

    private WindowManager getWindowManager() {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
