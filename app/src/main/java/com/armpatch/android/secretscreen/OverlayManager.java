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

    private ShadeOverlayWrapper shadeOverlayWrapper;
    private ControlsWrapper controlsWrapper;

    private int windowLayoutType;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }

        context = service;
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);

        shadeOverlayWrapper = new ShadeOverlayWrapper();
        controlsWrapper = new ControlsWrapper();
    }

    void start() {
        addViews();
        controlsWrapper.startRevealAnimation();
    }

    void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {
        windowManager.addView(controlsWrapper.controlsView, controlsWrapper.layoutParams);
        windowManager.addView(shadeOverlayWrapper.shadeLayout, shadeOverlayWrapper.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(shadeOverlayWrapper.shadeLayout, shadeOverlayWrapper.layoutParams);
        windowManager.updateViewLayout(controlsWrapper.controlsView, controlsWrapper.layoutParams);
    }

    private void removeViews() {
        windowManager.removeView(shadeOverlayWrapper.shadeLayout);
        windowManager.removeView(controlsWrapper.controlsView);
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

    class ShadeOverlayWrapper {
        View shadeLayout, shadeImage;

        WindowManager.LayoutParams layoutParams;
        private int PosYWhileHidden;

        ColorAnimator dimmerAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlayWrapper() {
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

        }

        private void hide() {

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
            final int DURATION_MS = 600;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void setDimmerOnTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                long lastTime;
                long duration;
                long MAX_DURATION = 200;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dimmerAnimator.start();

                            duration = System.currentTimeMillis() - lastTime;
                            if (duration < MAX_DURATION) {
                                startHideAnimation();
                                controlsWrapper.startRevealAnimation();
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
            updateWindowViewLayouts();
        }
    }

    class ControlsWrapper {
        View controlsView;
        ImageButton hideControlsButton, showOverlayButton;

        WindowManager.LayoutParams layoutParams;
        private int controlsXPosShown, controlsXPosHidden, OverlayPosYStart;

        @SuppressLint("ClickableViewAccessibility")
        ControlsWrapper() {
            layoutParams = getLayoutParams();
            controlsView = View.inflate(context, R.layout.ui_overlay_controls, null);

            calculateLayoutVariables();
            initButtons();

            layoutParams.x = controlsXPosShown;
            layoutParams.y = OverlayPosYStart;
        }

        private void calculateLayoutVariables(){
            controlsXPosShown = -1 * controlsView.findViewById(R.id.controls_frame)
                    .getLayoutParams().width;
            controlsXPosHidden = 0;
            OverlayPosYStart = (getDisplayHeight() / 2);
        }

        private void initButtons() {
            hideControlsButton = controlsView.findViewById(R.id.hide_controls_button);
            hideControlsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startHideAnimation();
                }
            });

            showOverlayButton = controlsView.findViewById(R.id.show_overlay_button);
            showOverlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shadeOverlayWrapper.startRevealAnimation();
                    startHideAnimation();
                }
            });
        }

        private void show() {
            windowManager.addView(controlsView, layoutParams);
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
            updateWindowViewLayouts();
        }
    }


}
