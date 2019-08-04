package com.armpatch.android.screenshade.overlays;

import com.armpatch.android.screenshade.services.OverlayService;


public class OverlayManager implements OverlayButton.ButtonCallbacks{

    private OverlayService overlayService;

    private OverlayShade overlayShade;
    private MovableButton movableButton;

    public OverlayManager(OverlayService overlayService) {
        this.overlayService = overlayService;
        initOverlays();
    }

    private void initOverlays(){
        //overlayShade = new OverlayShade(overlayService);
        movableButton = new MovableButton(overlayService);
    }

    public void showControls() {
        movableButton.reveal();
    }

    public void hideAllOverlays() {
        //overlayShade.hide();
        movableButton.hide();
    }

    @Override
    public void onShowShade() {
        overlayShade.show();
    }
}