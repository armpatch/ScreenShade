package com.armpatch.android.screenshade.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BaseInterpolator;
import android.view.animation.LinearInterpolator;

public class ShadeAnimator {

    private static final int REVEAL_DURATION = 400;
    private static final int HIDE_DURATION = 400;

    public static Animator getRevealAnimator(View shadeView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(shadeView, scaleX, scaleY);

        BaseInterpolator interpolator = new AccelerateInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(REVEAL_DURATION);

        return animator;
    }

    public static Animator getHideAnimator(View shadeView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(shadeView, scaleX, scaleY);

        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(HIDE_DURATION);

        return animator;
    }
}
