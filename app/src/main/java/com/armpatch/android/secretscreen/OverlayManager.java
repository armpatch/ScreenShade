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
import android.view.animation.DecelerateInterpolator;
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
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    class ShadeOverlay {
        View shadeLayout, shadeImage;

        WindowManager.LayoutParams layoutParams;
        private int PosYWhileHidden;

        private boolean isShown = false;

        ColorAnimator dimmerAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlay() {
            layoutParams = getLayoutParams();
            shadeLayout = View.inflate(context, R.layout.ui_overlay, null);
            shadeImage = shadeLayout.findViewById(R.id.shade);
            calculateLayoutVariables();

            dimmerAnimator = new ColorAnimator(shadeImage,
                    context.getColor(R.color.color_shade_normal),
                    context.getColor(R.color.color_shade_transparent));

            setDimmerOnTouchListener(shadeLayout);
            layoutParams.y = PosYWhileHidden;
            }

        private void show() {
            windowManager.addView(shadeLayout, layoutParams);
            isShown = true;
            startRevealAnimation();
            controlsOverlay.hide();
        }

        private void hide() {
            startHideAnimation();
            controlsOverlay.show();
        }

        private void startRevealAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = 0;
            final int DURATION_MS = 400;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new DecelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void startHideAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = PosYWhileHidden;
            final int DURATION_MS = 400;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);
            heightAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(shadeLayout);
                    isShown = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            heightAnimator.setInterpolator(new AccelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void setDimmerOnTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                long lastTime;
                long duration;
                long DOUBLE_TAP_DURATION = 400;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dimmerAnimator.start();

                            // double tap functionality
                            duration = System.currentTimeMillis() - lastTime;
                            if (duration < DOUBLE_TAP_DURATION) {
                                hide();
                            }
                            lastTime = System.currentTimeMillis();
                            break;

                        case MotionEvent.ACTION_UP:
                            dimmerAnimator.reverse();
                            break;

                        default:
                            break;
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
        boolean isShown = false;

        WindowManager.LayoutParams layoutParams;
        private int controlsXPosShown, controlsXPosHidden, OverlayPosYStart;

        @SuppressLint("ClickableViewAccessibility")
        ControlsOverlay() {
            layoutParams = getLayoutParams();
            controlsLayout = View.inflate(context, R.layout.ui_overlay_controls, null);

            calculateLayoutVariables();
            initButtons();

            layoutParams.x = controlsXPosShown;
            layoutParams.y = OverlayPosYStart;
        }

        private void calculateLayoutVariables(){
            controlsXPosShown = -1 * controlsLayout.findViewById(R.id.controls_frame)
                    .getLayoutParams().width;
            controlsXPosHidden = 0;
            OverlayPosYStart = (getDisplayHeight() / 2);
        }

        private void initButtons() {
            hideControlsButton = controlsLayout.findViewById(R.id.hide_controls_button);
            hideControlsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    ControlsNotification n = new ControlsNotification(context);
                    n.sendNotification();
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
            startHideAnimation();
        }

        private void startRevealAnimation() {
            float startingPosX = controlsXPosShown;
            float endingPosX = controlsXPosHidden;
            final int DURATION_MS = 200;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", startingPosX, endingPosX)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void startHideAnimation() {
            final int DURATION_MS = 200;

            ObjectAnimator XPositionAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsXPosHidden, controlsXPosShown)
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
