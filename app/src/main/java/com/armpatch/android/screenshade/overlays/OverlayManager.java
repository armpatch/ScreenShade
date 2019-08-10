package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;
import android.widget.Toast;

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

    public void hideAllOverlays() {
        //circularShade.hideToPoint();
        floatingButton.hide();
    }

    @Override
    public void onButtonClicked(Point centerPoint) {
        circularShade.revealFromPoint(centerPoint);
        Toast.makeText(service, "click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShadeRemoved() {

    }
}