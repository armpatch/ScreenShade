package com.armpatch.android.screenshade.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.OvershootInterpolator;

public class RevealAnimator {

    public static Animator get(View view) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha);

        BaseInterpolator interpolator = new OvershootInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(400);

        return animator;
    }
}
