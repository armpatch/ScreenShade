package com.armpatch.android.screenshade;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.notifications.ControlsNotification;
import com.armpatch.android.screenshade.services.OverlayService;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

public class OverlayManager {

    private static final String TAG = "OverlayManager";

    private final OverlayService context;
    private final WindowManager windowManager;

    private final ShadeOverlay shadeOverlay;
    private final ControlsOverlay controlsOverlay;

    private final int windowLayoutType;

    public OverlayManager(OverlayService service) {
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

    public void start() {
        controlsOverlay.startRevealAnimation();
    }

    public void stop() {
        if (shadeOverlay.isShown)
            shadeOverlay.hide();
        if (controlsOverlay.isShown)
            controlsOverlay.startHideAnimation(true);
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
        final View shadeLayout;
        final View shadeImage;

        final WindowManager.LayoutParams layoutParams;
        private int PosYWhileHidden;

        private boolean isShown = false;

        final DimmerAnimator dimmerAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlay() {
            layoutParams = getLayoutParams();
            shadeLayout = View.inflate(context, R.layout.overlay_shade_layout, null);
            shadeImage = shadeLayout.findViewById(R.id.shade);
            calculateLayoutVariables();


            dimmerAnimator = new DimmerAnimator(shadeImage,
                    context.getColor(R.color.color_shade_normal),
                    context.getColor(R.color.color_shade_dimmed));

            setCustomOnTouchListener(shadeLayout);
            layoutParams.y = PosYWhileHidden;
            }

        private void show() {
            windowManager.addView(shadeLayout, layoutParams);
            isShown = true;
            dimmerAnimator.makeOpaque();
            startSlideDownAnimation();
            controlsOverlay.startHideAnimation(false);
        }

        private void hide() {
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
                                Log.d(TAG, "dy = " + dy);
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

    class ControlsOverlay {
        final View controlsLayout;
        ImageButton hideControlsButton, showOverlayButton;
        boolean isShown;

        final WindowManager.LayoutParams layoutParams;
        private int controlsPosXOffScreen, controlsPosXOnScreen;
        private int controlsPosYOffScreen;

        @SuppressLint("ClickableViewAccessibility")
        ControlsOverlay() {
            layoutParams = getLayoutParams();
            controlsLayout = View.inflate(context, R.layout.overlay_controls_layout, null);

            calculateLayoutVariables();
            initButtons();

            layoutParams.x = controlsPosXOffScreen;
            layoutParams.y = controlsPosYOffScreen;
        }

        private void calculateLayoutVariables(){
            controlsPosXOffScreen = -1 * controlsLayout.findViewById(R.id.controls_frame)
                    .getLayoutParams().width;
            controlsPosXOnScreen = AnimationValues.X_OFFSET;
            controlsPosYOffScreen = (getDisplayHeight() / 2);
        }

        private void initButtons() {
            hideControlsButton = controlsLayout.findViewById(R.id.hide_controls_button);
            hideControlsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startHideAnimation(true);
                    ControlsNotification notification = new ControlsNotification(context);
                    notification.sendNotification();

                }
            });

            showOverlayButton = controlsLayout.findViewById(R.id.show_overlay_button);
            showOverlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shadeOverlay.show();
                    startHideAnimation(false);
                }
            });
        }

        private void startRevealAnimation() {
            windowManager.addView(controlsLayout, layoutParams);
            isShown = true;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsPosXOffScreen, controlsPosXOnScreen);

            heightAnimator.setDuration(AnimationValues.CONTROLS_REVEAL_TIME);
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

        private void startHideAnimation(final boolean stopService) {
            hideControlsButton.setClickable(false);
            showOverlayButton.setClickable(false);

            ObjectAnimator XPositionAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", controlsPosXOnScreen, controlsPosXOffScreen)
                    .setDuration(AnimationValues.CONTROLS_HIDE_TIME);

            XPositionAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    windowManager.removeView(controlsLayout);
                    isShown = false;
                    if (stopService) {
                        context.stopSelf();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });

            XPositionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            XPositionAnimator.start();
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
}
