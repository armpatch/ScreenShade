package com.armpatch.android.secretscreen;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import androidx.annotation.LayoutRes;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

class OverlayManager {

    private static final String TAG = "OverlayManager";

    private Service context;
    private WindowManager windowManager;

    private ShadeViewWrapper shadeViewWrapper;

    private int windowLayoutType;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }

        context = service;
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);

        shadeViewWrapper = new ShadeViewWrapper(R.layout.ui_plain_dark_shade);
    }

    void start() {
        addViews();
        shadeViewWrapper.startRevealAnimation();
    }

    void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {
        windowManager.addView(shadeViewWrapper.viewLayout, shadeViewWrapper.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(shadeViewWrapper.viewLayout, shadeViewWrapper.layoutParams);
    }

    private void removeViews() {
        windowManager.removeView(shadeViewWrapper.viewLayout);
    }

    private int getDisplayHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private int getNavBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    class ShadeViewWrapper {

        WindowManager.LayoutParams layoutParams;

        private int OVERLAY_POSY_HIDDEN;
        private int OVERLAY_POSY_VISIBLE = 0;
        private int overlayHeight = getDisplayHeight();

        private boolean animationEnded = false;

        View viewLayout, viewShade;

        @SuppressLint("ClickableViewAccessibility")
        ShadeViewWrapper(@LayoutRes int resource) {
            layoutParams = getLayoutParams();
            viewLayout = View.inflate(context, resource, null);
            viewShade = viewLayout.findViewById(R.id.shade);

            setLayoutDimensions();
            layoutParams.y = OVERLAY_POSY_HIDDEN;

            }

        private void startRevealAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(1000);

            heightAnimator.setInterpolator(new LinearInterpolator());

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet
                    .play(heightAnimator);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationEnded = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorSet.start();
        }

        private void setLayoutDimensions() {
            int height = getDisplayHeight() + getNavBarHeight();

            layoutParams.height = height;
            viewShade.getLayoutParams().height = height;
            OVERLAY_POSY_HIDDEN = -1 * height;
        }

        private WindowManager.LayoutParams getLayoutParams() {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.type = windowLayoutType;
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.TOP;

            return params;
        }

        private void setLayoutPosY(float y) {
            layoutParams.y = (int) y;
            updateWindowViewLayouts();
        }

    }
}
