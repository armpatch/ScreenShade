package com.armpatch.android.screenshade.overlay;

import android.graphics.Point;

import com.armpatch.android.screenshade.service.OverlayService;


public class OverlayManager implements ButtonOverlay.Callbacks, ShadeOverlay.Callbacks {

    OverlayService service;

    private ShadeOverlay shadeOverlay;
    private ButtonOverlay buttonOverlay;

    public OverlayManager(OverlayService service) {
        this.service = service;
        initOverlays();
    }

    public void start() {
        buttonOverlay.reveal();
    }

    public void stop() {
        shadeOverlay.hide();
        buttonOverlay.hide();
    }

    private void initOverlays(){
        shadeOverlay = new ShadeOverlay(this, service);
        buttonOverlay = new ButtonOverlay(this, service);
    }

    // Callback methods

    @Override
    public void onButtonClicked(Point center) {
        shadeOverlay.revealFromPoint(center);
}

    @Override
    public void onShadeRemoved() {
        buttonOverlay.reveal();
    }

    @Override
    public void onButtonTrashed() {
        service.stopSelf();
    }
}