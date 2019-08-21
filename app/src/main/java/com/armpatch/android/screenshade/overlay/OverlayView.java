package com.armpatch.android.screenshade.overlay;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

public abstract class OverlayView extends FrameLayout {

    abstract void onBackPressed();

    public OverlayView(Context context, @LayoutRes int resource, ViewGroup root) {
        super(context);
        initView(context, resource, root);
    }

    private void initView(Context context, @LayoutRes int resource, ViewGroup root) {
        this.addView(View.inflate(context, resource, root));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
