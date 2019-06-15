package com.armpatch.android.secretscreen;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

class ColorAnimator {

    private ObjectAnimator colorAnimator;

    ColorAnimator(View view, int colorStart, int colorEnd) {
        int DURATION = 200;

        colorAnimator = ObjectAnimator
                .ofInt(view, "backgroundColor", colorStart, colorEnd)
                .setDuration(DURATION);

        colorAnimator.setInterpolator(new AccelerateInterpolator());
        colorAnimator.setEvaluator(new ArgbEvaluator());
    }

    void start() {
        colorAnimator.start();
    }

    void reverse(){
        colorAnimator.reverse();
    }
}