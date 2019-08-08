package com.armpatch.android.screenshade.overlays;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

class WindowLayoutParams {

    @SuppressLint("RtlHardcoded")
    static LayoutParams getDefaultParams() {
        LayoutParams params = new LayoutParams();

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.type = DisplayInfo.getWindowLayoutType();
        params.flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;

        return params;
    }
}
