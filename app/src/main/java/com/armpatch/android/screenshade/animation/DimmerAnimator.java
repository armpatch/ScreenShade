package com.armpatch.android.screenshade.animation;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class DimmerAnimator {

    private final ObjectAnimator colorAnimator;
    private boolean viewIsTransparent;

    public DimmerAnimator(View view, int colorStart, int colorEnd) {

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd);

        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    public void makeTransparent() {
        if (!viewIsTransparent) {
            colorAnimator.setInterpolator(new AccelerateInterpolator());
            colorAnimator.setDuration(AnimationConstants.DIMMER_DIM_TIME);
            colorAnimator.start();
            // view.setBackgroundColor(colorEnd);
            viewIsTransparent = true;
        }
    }

    public void makeOpaque(){
        if (viewIsTransparent){
            colorAnimator.setInterpolator(new DecelerateInterpolator());
            colorAnimator.setDuration(AnimationConstants.DIMMER_UNDIM_TIME);
            colorAnimator.reverse();
            viewIsTransparent = false;
        }

    }

    public boolean isTransparent() {
        return viewIsTransparent;
    }
}