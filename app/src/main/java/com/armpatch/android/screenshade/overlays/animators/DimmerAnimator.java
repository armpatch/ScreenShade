package com.armpatch.android.screenshade.overlays.animators;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.LinearInterpolator;

public class DimmerAnimator {

    private static int DURATION = 200;
    private static float MIN_ALPHA = 0.7f;

    public static ObjectAnimator getDimmer(View view) {
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, MIN_ALPHA);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(DURATION);

        return animator;
    }

    public static ObjectAnimator getUnDimmer(View view) {

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, MIN_ALPHA);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(DURATION);

        return animator;
    }

    public static ObjectAnimator getAnimator(View view, float start, float end) {

        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, start, end);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(DURATION);

        return animator;
    }


}
