package com.armpatch.android.secretscreen;

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
        addViews();
        controlsOverlay.startRevealAnimation();
    }

    void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {
        windowManager.addView(controlsOverlay.controlsView, controlsOverlay.layoutParams);
        windowManager.addView(shadeOverlay.viewLayout, shadeOverlay.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(shadeOverlay.viewLayout, shadeOverlay.layoutParams);
        windowManager.updateViewLayout(controlsOverlay.controlsView, controlsOverlay.layoutParams);
    }

    private void removeViews() {
        windowManager.removeView(shadeOverlay.viewLayout);
        windowManager.removeView(controlsOverlay.controlsView);
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
        View viewLayout, viewShade;

        WindowManager.LayoutParams layoutParams;
        private int OVERLAY_POSY_HIDDEN;

        ColorAnimator colorAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlay() {
            layoutParams = getLayoutParams();
            viewLayout = View.inflate(context, R.layout.ui_overlay, null);
            viewShade = viewLayout.findViewById(R.id.shade);
            calculateLayoutVariables();

            colorAnimator = new ColorAnimator(viewShade,
                    context.getColor(R.color.color_shade_normal),
                    context.getColor(R.color.color_shade_transparent));

            setDimmerOnTouchListener(viewLayout);
            layoutParams.y = OVERLAY_POSY_HIDDEN;
            }

        private void startRevealAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = 0;
            final int DURATION_MS = 600;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void startHideAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = OVERLAY_POSY_HIDDEN;
            final int DURATION_MS = 600;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void setDimmerOnTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                long lastTime;
                long duration;
                long MAX_DURATION = 400;

                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            colorAnimator.start();

                            duration = System.currentTimeMillis() - lastTime;
                            if (duration < MAX_DURATION) {
                                startHideAnimation();
                                controlsOverlay.startRevealAnimation();
                            }
                            lastTime = System.currentTimeMillis();
                            break;

                        case MotionEvent.ACTION_UP:
                            colorAnimator.reverse();
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

        @SuppressWarnings("unused") //used with ObjectAnimator
        private void setLayoutPosY(float y) {
            layoutParams.y = (int) y;
            updateWindowViewLayouts();
        }
    }

    class ControlsOverlay {
        View controlsView;
        ImageButton hideControlsButton, showOverlayButton;

        WindowManager.LayoutParams layoutParams;
        private int OVERLAY_POSX_HIDDEN, OVERLAY_POSX_VISIBLE, OVERLAY_POSY_STARTING;

        @SuppressLint("ClickableViewAccessibility")
        ControlsOverlay() {
            layoutParams = getLayoutParams();
            controlsView = View.inflate(context, R.layout.ui_overlay_controls, null);

            calculateLayoutVariables();
            initButtons();

            layoutParams.x = OVERLAY_POSX_HIDDEN;
            layoutParams.y = OVERLAY_POSY_STARTING;
        }

        private void calculateLayoutVariables(){
            OVERLAY_POSX_HIDDEN = (int) (-1 *
                    controlsView.findViewById(R.id.controls_frame).getLayoutParams().width *
                    context.getResources().getDisplayMetrics().density);
            OVERLAY_POSX_VISIBLE = 0;
            OVERLAY_POSY_STARTING = (int) (getDisplayHeight() / 2);
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
                    shadeOverlay.startRevealAnimation();
                    startHideAnimation();
                }
            });
        }

        private void startRevealAnimation() {
            float startingPosX = OVERLAY_POSX_HIDDEN;
            float endingPosX = OVERLAY_POSX_VISIBLE;
            final int DURATION_MS = 600;
            final int START_DELAY_MS = 0;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", startingPosX, endingPosX)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void startHideAnimation() {
            float startingPosY = OVERLAY_POSX_VISIBLE;
            float endingPosY = OVERLAY_POSX_HIDDEN;
            final int DURATION_MS = 600;

            ObjectAnimator XPositionAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosX", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

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
