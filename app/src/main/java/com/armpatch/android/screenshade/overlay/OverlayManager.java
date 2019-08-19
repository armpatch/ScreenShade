package com.armpatch.android.screenshade.overlay;

import android.graphics.Point;

import com.armpatch.android.screenshade.service.OverlayService;


public class OverlayManager implements ButtonOverlay.Callbacks, ShadeOverlay.Callbacks {

    OverlayService service;

    private ShadeOverlay shadeOverlay;
    private ButtonOverlay buttonOverlay;

    public OverlayManager(OverlayService service) {
        this.service = service;

        shadeOverlay = new ShadeOverlay(this, service);
        buttonOverlay = new ButtonOverlay(this, service);
    }

    public void showOverlays() {
        buttonOverlay.startRevealAnimation();
    }

    public void hideOverlays() {
        shadeOverlay.hide();
        buttonOverlay.dismissButton();
    }

    @Override
    public void onButtonTapped(Point centerOfButton) {
        shadeOverlay.startRevealAnimationFromPoint(centerOfButton);
}

    @Override
    public void onShadeRemoved() {
        buttonOverlay.startRevealAnimation();
    }

    @Override
    public void onButtonDismissed() {
        service.stopSelf();
    }
}