package com.armpatch.android.screenshade.animation;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class TrashZoneAnimator {

    public static ObjectAnimator getRevealAnimator(View view) {
        return getAnimator(view, 0f, 1.0f);
    }

    public static ObjectAnimator getHideAnimator(View view) {
        return getAnimator(view, 1.0f, 0f);
    }

    private static ObjectAnimator getAnimator(View view, float start, float end) {
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, start, end);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);

        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(200);

        return animator;
    }

}
