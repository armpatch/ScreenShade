package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;

import com.armpatch.android.screenshade.services.OverlayService;


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
        shadeOverlay = new ShadeOverlay(this);
        buttonOverlay = new ButtonOverlay(this);
    }

    @Override
    public void onButtonClicked(Point centerPoint) {
        shadeOverlay.revealFromPoint(centerPoint);
}

    @Override
    public void onShadeRemoved(Point AnimationEndpoint) {

    }
}