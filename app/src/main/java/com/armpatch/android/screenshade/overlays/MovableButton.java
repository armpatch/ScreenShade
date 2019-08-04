package com.armpatch.android.screenshade.overlays;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.services.OverlayService;

public class MovableButton {

    private OverlayService service;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View viewLayout;
    private ImageButton button;

    private Point lastPosition;

    MovableButton (OverlayService service) {
        this.service = service;

        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = WindowLayoutParams.get(WindowLayoutParams.OPTION_1);

        inflateViews();
    }

    void reveal() {
        if (lastPosition == null) setDefaultPosition();

        addViewToWindowManager();
        startRevealAnimation();
    }

    void hide() {
        removeViewFromWindowManager();
    }

    private void startRevealAnimation() {

    }

    private void setDefaultPosition() {
        lastPosition = new Point(100,1000);

        layoutParams.x = lastPosition.x;
        layoutParams.y = lastPosition.y;
    }

    private void inflateViews() {
        viewLayout = View.inflate(service, R.layout.overlay_controls_new, null);
        button = viewLayout.findViewById(R.id.show_overlay_button);
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(viewLayout, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        windowManager.removeView(viewLayout);
    }

}
