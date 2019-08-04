package com.armpatch.android.screenshade.overlays;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.AnimationValues;
import com.armpatch.android.screenshade.notifications.ControlsNotification;
import com.armpatch.android.screenshade.services.OverlayService;

public class OverlayButton {

    OverlayService overlayService;
    ButtonCallbacks callbacks;
    WindowManager windowManager;

    View controlsLayout;
    ImageButton hideButton;
    ImageButton showButton;
    boolean isShown;

    WindowManager.LayoutParams layoutParams;
    private int controlsPosXOffScreen;
    private int controlsPosXOnScreen;
    private int controlsPosYOffScreen;

    interface ButtonCallbacks {
        void onShowShade();
    }

    @SuppressLint("ClickableViewAccessibility")
    OverlayButton(OverlayService overlayService) {
        this.overlayService = overlayService;

        callbacks = (ButtonCallbacks) overlayService;
        layoutParams = WindowLayoutParams.get(WindowLayoutParams.OPTION_1);
        controlsLayout = View.inflate(overlayService, R.layout.overlay_controls_layout, null);

        setPositionConstants();
        initOverlayButton();
    }

    private void setPositionConstants() {
        controlsPosXOffScreen = -1 * controlsLayout.findViewById(R.id.button_container_view)
                .getLayoutParams().width;
        controlsPosXOnScreen = AnimationValues.X_OFFSET;
        controlsPosYOffScreen = (DisplayInfo.getDisplayHeight(overlayService) / 2);

        layoutParams.x = controlsPosXOffScreen;
        layoutParams.y = controlsPosYOffScreen;
    }

    private void initOverlayButton() {
        hideButton = controlsLayout.findViewById(R.id.hide_controls_button);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHideAnimation(true);
                ControlsNotification notification = new ControlsNotification(overlayService);
                notification.sendNotification();

            }
        });

        showButton = controlsLayout.findViewById(R.id.button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onShowShade();
                startHideAnimation(false);
            }
        });
    }

    void startRevealAnimation() {
        addToWindowManager();
        createAndStartObjectAnimator();
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

    private void createAndStartObjectAnimator() {
        ObjectAnimator xPositionAnimator = ObjectAnimator
                .ofFloat(this, "LayoutPosX", controlsPosXOffScreen, controlsPosXOnScreen);

        xPositionAnimator.setDuration(AnimationValues.CONTROLS_REVEAL_TIME);
        xPositionAnimator.addListener(new Animator.AnimatorListener() {
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
        xPositionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        xPositionAnimator.start();
    }

    private void addToWindowManager() {
        WindowManager windowManager = (WindowManager) overlayService.getSystemService(Context.WINDOW_SERVICE);

        windowManager.addView(controlsLayout, layoutParams);
        isShown = true;
    }

    @SuppressWarnings("unused") //used with ObjectAnimator
    private void setLayoutPosX(float x) {
        layoutParams.x = (int) x;
        windowManager.updateViewLayout(controlsLayout, layoutParams);
    }
}
