package com.armpatch.android.screenshade.overlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.animation.TrashZoneAnimator;

class TrashZoneOverlay extends Overlay {

    private ObjectAnimator revealAnimator;
    private ObjectAnimator hideAnimator;

    TrashZoneOverlay(Context appContext) {
        super(appContext);

        DisplayInfo displayInfo = new DisplayInfo(appContext);

        windowManagerView = View.inflate(appContext, R.layout.trash_zone_overlay, null);
        windowManagerView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));

        setupAnimators();

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = displayInfo.getScreenWidth();
        layoutParams.height = displayInfo.getScreenHeight();

        layoutParams.y = displayInfo.getNavBarHeight();

        windowManagerView.setVisibility(View.INVISIBLE);
    }

    private void setupAnimators() {
        revealAnimator = TrashZoneAnimator.getRevealAnimator(windowManagerView);
        hideAnimator = TrashZoneAnimator.getHideAnimator(windowManagerView);
        hideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                windowManagerView.setVisibility(View.INVISIBLE);
                removeViewFromWindowManager();
            }
        });
    }

    void show() {
        if (View.INVISIBLE == windowManagerView.getVisibility()) {
            addViewToWindowManager();
            windowManagerView.setVisibility(View.VISIBLE);
            revealAnimator.start();
        }
    }

    void hide() {
        hideAnimator.start();
    }

}
