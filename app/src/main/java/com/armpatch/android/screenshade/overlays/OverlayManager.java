package com.armpatch.android.screenshade.overlays;

import com.armpatch.android.screenshade.services.OverlayService;


public class OverlayManager implements OverlayButton.ButtonCallbacks{

    private OverlayService overlayService;

    private OverlayShade overlayShade;
    private OverlayButton overlayButton;


    public OverlayManager(OverlayService overlayService) {
        this.overlayService = overlayService;
        instantiateOverlayObjects();
    }

    void instantiateOverlayObjects(){
        overlayShade = new OverlayShade(overlayService);
        overlayButton = new OverlayButton(overlayService);
    }

    public void showOverlayControls() {
        overlayButton.startRevealAnimation();
    }

    public void hideAllOverlays() {
        overlayShade.hide();
        overlayButton.startHideAnimation(true);
    }

    @Override
    public void onShowShade() {
        overlayShade.show();
    }
}
