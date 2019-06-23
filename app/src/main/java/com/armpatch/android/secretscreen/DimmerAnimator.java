package com.armpatch.android.secretscreen;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class DimmerAnimator {

    private ObjectAnimator colorAnimator;

    DimmerAnimator(View view, int colorStart, int colorEnd) {
        int DURATION = 200;

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd)
                .setDuration(DURATION);

        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    void makeTransparent() {
        colorAnimator.setInterpolator(new AccelerateInterpolator());
        colorAnimator.start();
    }

    void makeOpaque(){
        colorAnimator.setInterpolator(new DecelerateInterpolator());
        colorAnimator.reverse();
    }
}