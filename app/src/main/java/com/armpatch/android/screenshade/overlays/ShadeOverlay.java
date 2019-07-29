package com.armpatch.android.screenshade.overlays;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.services.OverlayService;

public class ShadeOverlay {
    final View shadeLayout;
    final View shadeImage;

    final WindowManager.LayoutParams layoutParams;
    private int PosYWhileHidden;

    public boolean isShown = false;

    final DimmerAnimator dimmerAnimator;

    @SuppressLint("ClickableViewAccessibility")
    ShadeOverlay(OverlayService overlayService) {
        layoutParams = getLayoutParams();

        shadeLayout = View.inflate(overlayService, R.layout.overlay_shade_layout, null);
        shadeImage = shadeLayout.findViewById(R.id.shade);
        calculateLayoutVariables();

        dimmerAnimator = new DimmerAnimator(shadeImage,
                overlayService.getColor(R.color.color_shade_normal),
                overlayService.getColor(R.color.color_shade_dimmed));

        setCustomOnTouchListener(shadeLayout);
        layoutParams.y = PosYWhileHidden;
    }

    void show() {
        windowManager.addView(shadeLayout, layoutParams);
        isShown = true;
        dimmerAnimator.makeOpaque();
        startSlideDownAnimation();
        controlsOverlay.startHideAnimation(false);
    }

    void hide() {
        startSlideUpAnimation();
    }

    private void startSlideDownAnimation() {

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(this, "LayoutPosY", layoutParams.y, 0)
                .setDuration(AnimationValues.SLIDE_DOWN_ANIMATION);

        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {shadeLayout.setClickable(false);}

            @Override
            public void onAnimationEnd(Animator animation) {shadeLayout.setClickable(true);}

            @Override
            public void onAnimationCancel(Animator animation) { }
            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        heightAnimator.start();
    }

    private void startSlideUpAnimation() {

        final ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(this, "LayoutPosY", layoutParams.y, PosYWhileHidden)
                .setDuration(AnimationValues.SLIDE_UP_ANIMATION);

        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                dimmerAnimator.makeOpaque();
                shadeLayout.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                windowManager.removeView(shadeLayout);
                isShown = false;
                controlsOverlay.startRevealAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        heightAnimator.setInterpolator(new AccelerateInterpolator());
        heightAnimator.start();
    }

    private void setCustomOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float lastTouchY;
            float dy;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (shadeLayout.isClickable()){

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN: {
                            dy = 0;
                            lastTouchY = event.getRawY();
                            break;
                        }

                        case MotionEvent.ACTION_MOVE: {
                            final float y = event.getRawY();
                            dy = y - lastTouchY;
                            lastTouchY = y;

                            if (dy < - AnimationValues.OVERLAY_DRAG_UP_SPEED) {
                                hide();
                                break;
                            }
                            break;
                        }

                        case MotionEvent.ACTION_UP: {

                            if (!dimmerAnimator.isTransparent()) {
                                dimmerAnimator.makeTransparent();
                            } else {
                                dimmerAnimator.makeOpaque();
                            }
                            break;
                        }

                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    private void calculateLayoutVariables() {
        int overlayHeight = getDisplayHeight() + getNavBarHeight();

        layoutParams.height = overlayHeight;
        shadeImage.getLayoutParams().height = overlayHeight;
        PosYWhileHidden = -1 * overlayHeight;
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

    @SuppressWarnings("unused") //used with ObjectAnimator
    private void setLayoutPosY(float y) {
        layoutParams.y = (int) y;
        windowManager.updateViewLayout(shadeLayout, layoutParams);
    }
}