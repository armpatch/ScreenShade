package com.armpatch.android.secretscreen;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class DimmerAnimator {

    private ObjectAnimator colorAnimator;
    private View view;
    private boolean isTransparent;

    DimmerAnimator(View view, int colorStart, int colorEnd) {
        this.view = view;
        int DURATION = 200;

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd)
                .setDuration(DURATION);

        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    void makeTransparent() {
        if (!isTransparent) {
            colorAnimator.setInterpolator(new AccelerateInterpolator());
            colorAnimator.start();
            isTransparent = true;
        }
    }

    void makeOpaque(){
        if (isTransparent){
            colorAnimator.setInterpolator(new DecelerateInterpolator());
            colorAnimator.reverse();
            isTransparent = false;
        }

    }
}