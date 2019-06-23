package com.armpatch.android.secretscreen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

class OverlayManager {

    private static final String TAG = "OverlayManager";

    private Service context;
    private WindowManager windowManager;

    private ShadeOverlay shadeOverlay;
    private ControlsOverlay controlsOverlay;

    private int windowLayoutType;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }

        context = service;
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);

        shadeOverlay = new ShadeOverlay();
        controlsOverlay = new ControlsOverlay();
    }

    void start() {
        controlsOverlay.show();
    }

    void stop() {
        if (shadeOverlay.isShown)
            shadeOverlay.hide();
        if (controlsOverlay.isShown)
            controlsOverlay.hide();
    }

    private int getDisplayHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private int getNavBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        return (resourceId > 0)?  resources.getDimensionPixelSize(resourceId): 0;
    }

    class ShadeOverlay {
        View shadeLayout, shadeImage;

        WindowManager.LayoutParams layoutParams;
        private int PosYWhileHidden;

        private boolean isShown = false;
        boolean isDimmed = false;

        DimmerAnimator dimmerAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlay() {
            layoutParams = getLayoutParams();
            shadeLayout = View.inflate(context, R.layout.ui_overlay, null);
            shadeImage = shadeLayout.findViewById(R.id.shade);
            calculateLayoutVariables();


            dimmerAnimator = new DimmerAnimator(shadeImage,
                    context.getColor(R.color.color_shade_normal),
                    context.getColor(R.color.color_shade_transparent));

            setCustomOnTouchListener(shadeLayout);
            layoutParams.y = PosYWhileHidden;
            }

        private void show() {
            windowManager.addView(shadeLayout, layoutParams);
            isShown = true;
            dimmerAnimator.makeOpaque();
            isDimmed = false;
            startSlideDownAnimation();
            controlsOverlay.hide();
        }

        private void hide() {
            startSlideUpAnimation();
            controlsOverlay.show();
        }

        private void startSlideDownAnimation() {
            final int DURATION_MS = 600;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", layoutParams.y, 0)
                    .setDuration(DURATION_MS);

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
            final int DURATION_MS = 400;

            final ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", layoutParams.y, PosYWhileHidden)
                    .setDuration(DURATION_MS);

            heightAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    dimmerAnimator.makeOpaque();
                    isDimmed = false;
                    shadeLayout.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(shadeLayout);
                    isShown = false;
                    heightAnimator.removeAllListeners();
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
                long lastTouchClockTime;
                long timeSinceLastTouch;
                long DOUBLE_TAP_DURATION = 400;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (shadeLayout.isClickable()){
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // double tap functionality
                                timeSinceLastTouch = System.currentTimeMillis() - lastTouchClockTime;
                                if (timeSinceLastTouch < DOUBLE_TAP_DURATION)
                                    hide();
                                lastTouchClockTime = System.currentTimeMillis();
                                break;

                            case MotionEvent.ACTION_UP:
                                if (!isDimmed) {
                                    dimmerAnimator.makeTransparent();
                                    isDimmed = true;
                                } else if (isDimmed){
                                    dimmerAnimator.makeOpaque();
                                    isDimmed = false;
                                }
                                break;

                            default:
                                break;
                        }
                    }

                    return false;
                }
            });
        }

        private void calculateLayoutVariables() {
            int height = getDisplayHeight() + getNavBarHeight();

            layoutParams.height = height;
            shadeImage.getLayoutParams().height = height;
            PosYWhileHidden = -1 * height;
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

    class ControlsOverlay {
        View controlsLayout;
        ImageButton hideControlsButton, showOverlayButton;
        boolean isShown;

        WindowManager.LayoutParams layoutParams;
        private int controlsPosXOnScreen, controlsPosXOffScreen, controlsPosYOffScreen;

        @SuppressLint("ClickableViewAccessibility")
        ControlsOverlay() {
            layoutParams = getLayoutParams();
            controlsLayout = View.inflate(context, R.layout.ui_overlay_controls, null);

            calculateLayoutVariables();
            initButtons();

            layoutParams.x = controlsPosXOnScreen;
            layoutParams.y = controlsPosYOffScreen;
        }

        private void calculateLayoutVariables(){
            controlsPosXOnScreen = -1 * controlsLayout.findViewById(R.id.controls_frame)
                    .getLayoutParams().width;
            controlsPosXOffScreen = 0;
            controlsPosYOffScreen = (getDisplayHeight() / 2);
        }

        private void initButtons() {
            hideControlsButton = controlsLayout.findViewById(R.id.hide_controls_button);
            hideControlsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    ControlsNotification notification = new ControlsNotification(context);
                    notification.sendNotification();
                }
            });

            showOverlayButton = controlsLayout.findViewById(R.id.show_overlay_button);
            showOverlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shadeOverlay.show();
                    hide();
                }
            });
        }

        private void show() {
            windowManager.addView(controlsLayout, layoutParams);
            isShown = true;
            startRevealAnimation();
        }

        private void hide() {
            hideControlsButton.setClickable(false);
            showOverlayButton.setClickable(false);
            startHideAnimation();
        }

        private void startRevealAnimation() {
            final int DURATION_MS = 200;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsPosXOnScreen, controlsPosXOffScreen);

            heightAnimator.setDuration(DURATION_MS);
            heightAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hideControlsButton.setClickable(true);
                    showOverlayButton.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            heightAnimator.start();
        }

        private void startHideAnimation() {
            final int DURATION_MS = 200;

            ObjectAnimator XPositionAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsPosXOffScreen, controlsPosXOnScreen)
                    .setDuration(DURATION_MS);

            XPositionAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(controlsLayout);
                    isShown = false;
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
}
