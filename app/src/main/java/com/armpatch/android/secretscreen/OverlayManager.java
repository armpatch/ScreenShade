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

import androidx.annotation.LayoutRes;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

class OverlayManager {

    private static final String TAG = "OverlayManager";

    private Service context;
    private WindowManager windowManager;

    private ShadeOverlay shadeOverlay;

    private int windowLayoutType;

    OverlayManager(Service service) {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }

        context = service;
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        shadeOverlay = new ShadeOverlay(R.layout.ui_overlay);
    }

    void start() {
        addViews();
        shadeOverlay.startSlideDownAnimation();
    }

    void stop() {
        removeViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViews() {
        windowManager.addView(shadeOverlay.viewLayout, shadeOverlay.layoutParams);
    }

    private void updateWindowViewLayouts() {
        windowManager.updateViewLayout(shadeOverlay.viewLayout, shadeOverlay.layoutParams);
    }

    private void removeViews() {
        windowManager.removeView(shadeOverlay.viewLayout);
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

        WindowManager.LayoutParams layoutParams;
        private int OVERLAY_POSY_HIDDEN;

        View viewLayout, viewShade;
        ColorAnimator colorAnimator;

        @SuppressLint("ClickableViewAccessibility")
        ShadeOverlay(@LayoutRes int resource) {
            layoutParams = getLayoutParams();
            viewLayout = View.inflate(context, resource, null);
            viewShade = viewLayout.findViewById(R.id.shade);

            colorAnimator = new ColorAnimator(viewShade,
                    context.getColor(R.color.color_shade_normal),
                    context.getColor(R.color.color_shade_transparent));

            setDimmerOnTouchListener(viewLayout);
            setLayoutDimensions();
            layoutParams.y = OVERLAY_POSY_HIDDEN;
            }

        private void startSlideDownAnimation() {
            float startingPosY = layoutParams.y;
            float endingPosY = 0;
            final int DURATION_MS = 600;
            final int START_DELAY_MS = 400;

            ObjectAnimator heightAnimator = ObjectAnimator
                    .ofFloat(this, "LayoutPosY", startingPosY, endingPosY)
                    .setDuration(DURATION_MS);

            heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            heightAnimator.setStartDelay(START_DELAY_MS);
            heightAnimator.start();
        }

        private void setDimmerOnTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            colorAnimator.start();
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
