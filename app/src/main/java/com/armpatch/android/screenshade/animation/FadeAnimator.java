package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.LinearInterpolator;

public class FadeAnimator {

    public static ObjectAnimator getFadeAnimator(View v) {
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, v.getAlpha(), 0.0f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(v, alpha);

        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(200);

        return animator;
    }

}
