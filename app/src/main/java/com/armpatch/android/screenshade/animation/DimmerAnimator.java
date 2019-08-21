package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.LinearInterpolator;

public class DimmerAnimator {

    public static ObjectAnimator getAnimator(View view, float start, float end) {

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, start, end);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        int DURATION = 200;
        animator.setDuration(DURATION);

        return animator;
    }


}
