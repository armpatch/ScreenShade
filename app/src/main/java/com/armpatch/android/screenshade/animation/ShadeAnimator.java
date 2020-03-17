package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class ShadeAnimator {

    private static final int REVEAL_DURATION = 400;
    private static final int HIDE_DURATION = 400;

    private static final float MIN_SIZE = 0f;
    private static final float MIN_ALPHA = 0.6f;

    public static ObjectAnimator getRevealAnimatorSet(View shadeView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, MIN_SIZE, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, MIN_SIZE, 1f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, MIN_ALPHA, 1f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(shadeView, scaleX, scaleY, alpha);

        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(REVEAL_DURATION);

        return animator;
    }

    public static ObjectAnimator getHideAnimator(View shadeView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, MIN_SIZE);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, MIN_SIZE);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, MIN_ALPHA);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(shadeView, alpha, scaleX, scaleY);

        BaseInterpolator interpolator = new DecelerateInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(HIDE_DURATION);

        return animator;
    }
}
