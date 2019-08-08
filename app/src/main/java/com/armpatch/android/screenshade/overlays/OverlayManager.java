package com.armpatch.android.screenshade.overlays;

import android.widget.Toast;

import com.armpatch.android.screenshade.services.OverlayService;


public class OverlayManager implements MovableButton.Callbacks, OverlayShade.Callbacks {

    OverlayService service;

    private OverlayShade overlayShade;
    private MovableButton movableButton;

    public OverlayManager(OverlayService service) {
        this.service = service;
        initOverlays();
    }

    private void initOverlays(){
        overlayShade = new OverlayShade(this);
        movableButton = new MovableButton(this);
    }

    public void showMovableButton() {
        movableButton.reveal();
    }

    public void hideAllOverlays() {
        //overlayShade.hide();
        movableButton.hide();
    }

    @Override
    public void onButtonClicked() {
        overlayShade.reveal();
        Toast.makeText(service, "click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShadeRemoved() {

    }
}