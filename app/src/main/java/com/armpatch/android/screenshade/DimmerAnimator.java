package com.armpatch.android.screenshade;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class DimmerAnimator {

    private final ObjectAnimator colorAnimator;
    private boolean viewIsTransparent;

    DimmerAnimator(View view, int colorStart, int colorEnd) {

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd);

        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    void makeTransparent() {
        if (!viewIsTransparent) {
            colorAnimator.setInterpolator(new AccelerateInterpolator());
            colorAnimator.setDuration(AnimationValues.DIMMER_DIM_TIME);
            colorAnimator.start();
            // view.setBackgroundColor(colorEnd);
            viewIsTransparent = true;
        }
    }

    void makeOpaque(){
        if (viewIsTransparent){
            colorAnimator.setInterpolator(new DecelerateInterpolator());
            colorAnimator.setDuration(AnimationValues.DIMMER_UNDIM_TIME);
            colorAnimator.reverse();
            viewIsTransparent = false;
        }

    }

    boolean isTransparent() {
        return viewIsTransparent;
    }
}