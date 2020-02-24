package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class DimmerAnimator {

    private static int DIM_TIME = 200;
    private static int DELAY = 50;

    public static ObjectAnimator getAnimator(View view, float startingAlpha, float finalAlpha) {

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, startingAlpha, finalAlpha);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);

        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(DELAY);
        animator.setDuration(DIM_TIME);

        return animator;
    }
}
