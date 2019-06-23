package com.armpatch.android.secretscreen;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class DimmerAnimator {

    private ObjectAnimator colorAnimator;
    private boolean viewIsTransparent;

    DimmerAnimator(View view, int colorStart, int colorEnd) {
        int DURATION = 200;

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd)
                .setDuration(DURATION);

        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    void makeTransparent() {
        if (!viewIsTransparent) {
            colorAnimator.setInterpolator(new AccelerateInterpolator());
            colorAnimator.start();
            viewIsTransparent = true;
        }
    }

    void makeOpaque(){
        if (viewIsTransparent){
            colorAnimator.setInterpolator(new DecelerateInterpolator());
            colorAnimator.reverse();
            viewIsTransparent = false;
        }

    }

    boolean isTransparent() {
        return viewIsTransparent;
    }
}