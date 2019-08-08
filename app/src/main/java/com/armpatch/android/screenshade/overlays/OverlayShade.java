package com.armpatch.android.screenshade.overlays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.services.OverlayService;

public class OverlayShade {

    private OverlayService service;
    private WindowManager windowManager;
    private Callbacks callbacks;

    private View shadeLayout;
    private View shadeImage;

    private WindowManager.LayoutParams layoutParams;

    private boolean isShown = false;

    interface Callbacks {
        void onShadeRemoved();
    }

    @SuppressLint("ClickableViewAccessibility")
    OverlayShade(OverlayService service) {
        this.service = service;
        windowManager = getWindowManager();

        inflateViews();
        setLayoutParams();

    }

    void show() {
        addViewToWindowManager();
        callbacks.onShadeRemoved();
    }

    void hide() {
        if (isShown) {}
    }

    private void inflateViews() {
        shadeLayout = View.inflate(service, R.layout.overlay_shade_layout, null);
        shadeImage = shadeLayout.findViewById(R.id.shade_circle);
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

    private WindowManager getWindowManager() {
        return (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }
}
