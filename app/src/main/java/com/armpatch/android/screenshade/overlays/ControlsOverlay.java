package com.armpatch.android.screenshade.overlays;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.notifications.ControlsNotification;

public class ControlsOverlay {

    final View controlsLayout;
    ImageButton hideButton, showButton;
    boolean isShown;

    final WindowManager.LayoutParams layoutParams;
    private int controlsPosXOffScreen, controlsPosXOnScreen;
    private int controlsPosYOffScreen;

    @SuppressLint("ClickableViewAccessibility")
    ControlsOverlay() {
        layoutParams = getLayoutParams();
        controlsLayout = View.inflate(overlayService, R.layout.overlay_controls_layout, null);

        calculateLayoutVariables();
        initButtons();

        layoutParams.x = controlsPosXOffScreen;
        layoutParams.y = controlsPosYOffScreen;
    }

    private void calculateLayoutVariables() {
        controlsPosXOffScreen = -1 * controlsLayout.findViewById(R.id.controls_frame)
                .getLayoutParams().width;
        controlsPosXOnScreen = AnimationValues.X_OFFSET;
        controlsPosYOffScreen = (getDisplayHeight() / 2);
    }

    private void initButtons() {
        hideButton = controlsLayout.findViewById(R.id.hide_controls_button);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHideAnimation(true);
                ControlsNotification notification = new ControlsNotification(overlayService);
                notification.sendNotification();

            }
        });

        showButton = controlsLayout.findViewById(R.id.show_overlay_button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shadeOverlay.show();
                startHideAnimation(false);
            }
        });
    }

    void startRevealAnimation() {
        windowManager.addView(controlsLayout, layoutParams);
        isShown = true;

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(this, "LayoutPosX", controlsPosXOffScreen, controlsPosXOnScreen);

        heightAnimator.setDuration(AnimationValues.CONTROLS_REVEAL_TIME);
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideButton.setClickable(true);
                showButton.setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.start();
    }

    void startHideAnimation(final boolean stopService) {
        if (isShown) {
            hideButton.setClickable(false);
            showButton.setClickable(false);

            ObjectAnimator XPositionAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsPosXOnScreen, controlsPosXOffScreen)
                    .setDuration(AnimationValues.CONTROLS_HIDE_TIME);

            XPositionAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(controlsLayout);
                    isShown = false;
                    if (stopService) {
                        overlayService.stopSelf();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            XPositionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            XPositionAnimator.start();
        }
    }

    @SuppressLint("RtlHardcoded")
    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = windowLayoutType;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;

        return params;
    }

    @SuppressWarnings("unused") //used with ObjectAnimator
    private void setLayoutPosX(float x) {
        layoutParams.x = (int) x;
        windowManager.updateViewLayout(controlsLayout, layoutParams);
    }
}
