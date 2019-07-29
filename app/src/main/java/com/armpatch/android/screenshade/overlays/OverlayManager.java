package com.armpatch.android.screenshade.overlays;

import android.os.Build;

import com.armpatch.android.screenshade.services.OverlayService;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

public class OverlayManager {

    private OverlayService overlayService;

    private ShadeOverlay shadeOverlay;
    private ControlsOverlay controlsOverlay;
    private Callbacks callbacks;

    interface Callbacks {
        void hideScreen();
        void on
    }

    public OverlayManager(OverlayService overlayService) {
        this.overlayService = overlayService;

        initOverlays();
    }

    public void startOverlay() {
        controlsOverlay.startRevealAnimation();
    }

    public void stopOverlay() {
        if (shadeOverlay.isShown)
            shadeOverlay.hide();
        if (controlsOverlay.isShown)
            controlsOverlay.startHideAnimation(true);
    }

    private int getWindowLayoutType() {
        int windowLayoutType;
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }
        return windowLayoutType;
    }

    void initOverlays(){
        shadeOverlay = new ShadeOverlay(overlayService);
        controlsOverlay = new ControlsOverlay();
    }
}
