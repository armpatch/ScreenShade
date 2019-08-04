package com.armpatch.android.screenshade.overlays;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.RevealAnimator;
import com.armpatch.android.screenshade.services.OverlayService;

class MovableButton {

    private OverlayService service;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View buttonLayout;
    private ImageButton button;

    private Point savedPosition;

    MovableButton (OverlayService service) {
        this.service = service;

        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        inflateViews();
        setLayoutParams();
    }

    private void setLayoutParams() {
        layoutParams = WindowLayoutParams.get(WindowLayoutParams.OPTION_1);

        View buttonContainer = buttonLayout.findViewById(R.id.button_container_view);

        int width = buttonContainer.getLayoutParams().width;
        int height = buttonContainer.getLayoutParams().height;

        layoutParams.width = (int) (width * 1.2);
        layoutParams.height = (int) (height * 1.2);
    }

    void reveal() {
        if (savedPosition == null) setPositionToDefault();

        addViewToWindowManager();
        startRevealAnimation();
    }

    void hide() {
        removeViewFromWindowManager();
    }

    private void inflateViews() {
        buttonLayout = View.inflate(service, R.layout.movable_button, null);
        button = buttonLayout.findViewById(R.id.button);
    }

    private void startRevealAnimation() {
        RevealAnimator.getAnimatorForView(buttonLayout).start();
    }

    private void setPositionToDefault() {
        savedPosition = new Point(100,1000);

        layoutParams.x = savedPosition.x;
        layoutParams.y = savedPosition.y;
    }

    private void addViewToWindowManager() {
        try {
            windowManager.addView(buttonLayout, layoutParams);
        } catch ( WindowManager.BadTokenException e) {
            Log.e("TAG", "View already added to WindowManager.", e);
        }
    }

    private void removeViewFromWindowManager() {
        windowManager.removeView(buttonLayout);
    }

}
