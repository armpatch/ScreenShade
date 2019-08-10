package com.armpatch.android.screenshade.overlays;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ShadeAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

@SuppressLint("ClickableViewAccessibility")
class CircularShade {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;

    private View floatingShade;
    private View circleImageView;
    Point buttonPoint;

    private WindowManager.LayoutParams layoutParams;

    interface Callbacks {
        void onShadeRemoved(Point AnimationEndpoint);
    }

    CircularShade(OverlayManager overlayManager) {
        this.service = overlayManager.service;
        windowManager = getWindowManager();

        callbacks = overlayManager;

        inflateViews();
        setWindowLayoutParams();
        calculateExpandedCircleDimensions();
    }

    void revealFromPoint(Point centerPoint) {
        buttonPoint = centerPoint;
        addViewToWindowManager();
        setAnimationCenter(centerPoint);
        ShadeAnimator.getRevealAnimator(circleImageView).start();
    }

    void hideToPoint(Point point) {
        setAnimationCenter(point);
        Animator animator = ShadeAnimator.getHideAnimator(circleImageView);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeViewFromWindowManager();
            }
        });
        animator.start();
    }



    private void inflateViews() {
        floatingShade = View.inflate(service, R.layout.floating_shade, null);
        circleImageView = floatingShade.findViewById(R.id.shade_circle);

        setOnTouchListener(floatingShade);
    }

    private void setOnTouchListener(View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            int DOUBLE_TAP_DURATION = 300;
            long duration;
            long lastTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                event.setLocation(event.getRawX(), event.getRawY());

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        duration = System.currentTimeMillis() - lastTime;
                        if (duration < DOUBLE_TAP_DURATION) {
                            Point point = new Point((int)event.getRawX(), (int)event.getRawY());
                            hideToPoint(buttonPoint);
                        }
                        lastTime = System.currentTimeMillis();
                        break;
                    }
                }
                return false;
            }
        });
    }

    private void setWindowLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();

        layoutParams.height = Display.getHeight(service) + Display.getNavBarHeight(service);
    }

    private void calculateExpandedCircleDimensions() {
        int diameter = 2 * ( Display.getDiagonal(service) + Display.getNavBarHeight(service));

        circleImageView.getLayoutParams().height = diameter;
        circleImageView.getLayoutParams().width = diameter;
    }

    private void setAnimationCenter(Point origin) {
        Point offsetPoint = CoordinateMaker.getCenterShiftedPoint(circleImageView, origin);

        circleImageView.setX(offsetPoint.x);
        circleImageView.setY(offsetPoint.y);
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(floatingShade, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        windowManager.removeView(floatingShade);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
