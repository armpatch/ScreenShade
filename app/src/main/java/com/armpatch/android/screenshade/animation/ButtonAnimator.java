package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.BaseInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class ButtonAnimator {

    private static int REVEAL_DURATION = 400;
    private static int HIDE_DURATION = 200;

    public static ObjectAnimator getRevealAnimator(View buttonView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(buttonView, scaleX, scaleY, alpha);

        BaseInterpolator interpolator = new OvershootInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(REVEAL_DURATION);

        return animator;
    }

    public static ObjectAnimator getHideAnimator(View buttonView) {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.0f);

        ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(buttonView, scaleX, scaleY, alpha);

        BaseInterpolator interpolator = new LinearInterpolator();

        animator.setInterpolator(interpolator);
        animator.setDuration(HIDE_DURATION);

        return animator;
    }
}