package com.armpatch.android.screenshade.overlays;

import com.armpatch.android.screenshade.services.OverlayService;


public class OverlayManager {

    private OverlayService overlayService;

    private ShadeOverlay shadeOverlay;
    private ControlsOverlay controlsOverlay;
    private Callbacks callbacks;

    interface Callbacks {
        void onHideShadeRequested();
        void onHideControlsRequested();
    }

    public OverlayManager(OverlayService overlayService) {
        this.overlayService = overlayService;
        callbacks = (Callbacks) this;
        instantiateOverlayObjects();
    }

    public void showOverlayControls() {
        controlsOverlay.startRevealAnimation();
    }

    public void hideAllOverlays() {
        shadeOverlay.hide();
        controlsOverlay.startHideAnimation(true);
    }

    void instantiateOverlayObjects(){
        shadeOverlay = new ShadeOverlay(overlayService);
        controlsOverlay = new ControlsOverlay();
    }
}
