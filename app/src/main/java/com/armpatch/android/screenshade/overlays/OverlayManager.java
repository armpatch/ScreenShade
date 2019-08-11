package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;

import com.armpatch.android.screenshade.services.OverlayService;


public class OverlayManager implements FloatingButton.Callbacks, CircularShade.Callbacks {

    OverlayService service;

    private CircularShade circularShade;
    private FloatingButton floatingButton;

    public OverlayManager(OverlayService service) {
        this.service = service;
        initOverlays();
    }

    private void initOverlays(){
        circularShade = new CircularShade(this);
        floatingButton = new FloatingButton(this);
    }

    public void revealMovableButton() {
        floatingButton.reveal();
    }

    public void hideAllOverlays() { //TODO crashes if overlays have not been started yet
        circularShade.hide();
        floatingButton.hide();
    }

    @Override
    public void onButtonClicked(Point centerPoint) {
        circularShade.revealFromPoint(centerPoint);
}

    @Override
    public void onShadeRemoved(Point AnimationEndpoint) {

    }
}