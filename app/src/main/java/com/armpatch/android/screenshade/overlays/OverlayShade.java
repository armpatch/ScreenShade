package com.armpatch.android.screenshade.overlays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.ShadeAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

public class OverlayShade {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;

    private View shadeLayout;
    private View shadeCircle;

    private WindowManager.LayoutParams layoutParams;

    private boolean isShown = false;

    interface Callbacks {
        void onShadeRemoved();
    }

    @SuppressLint("ClickableViewAccessibility")
    OverlayShade(OverlayManager overlayManager) {
        this.service = overlayManager.service;
        windowManager = getWindowManager();

        callbacks = (Callbacks) overlayManager;

        inflateViews();
        setLayoutParams();

    }

    void reveal() {
        addViewToWindowManager();
        startRevealAnimation();
    }

    void hide() {
        startHideAnimation();
    }

    private void inflateViews() {
        shadeLayout = View.inflate(service, R.layout.overlay_shade_layout, null);
        shadeCircle = shadeLayout.findViewById(R.id.shade_circle);
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(shadeLayout, layoutParams);
            isShown = true;
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void setLayoutParams() {
        layoutParams = WindowLayoutParams.getDefaultParams();

        layoutParams.height = DisplayInfo.getDisplayHeight(service) +
                DisplayInfo.getNavBarHeight(service);
    }

    private void startRevealAnimation() {
        ShadeAnimator.getRevealAnimator(shadeCircle).start();
    }

    private void startHideAnimation() {
        ShadeAnimator.getHideAnimator(shadeCircle).start();
    }

    private WindowManager getWindowManager() {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
