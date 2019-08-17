package com.armpatch.android.screenshade.overlay;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

class WindowLayoutParams {

    @SuppressLint("RtlHardcoded")
    static LayoutParams getDefaultParams() {
        LayoutParams params = new LayoutParams();

        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.type = getWindowLayoutType();
        params.flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;

        return params;
    }

    private static int getWindowLayoutType() {
        int windowLayoutType;
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = TYPE_PHONE;
        }
        return windowLayoutType;
    }
}
