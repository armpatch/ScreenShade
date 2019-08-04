package com.armpatch.android.screenshade.overlays;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

class WindowLayoutParams {

    public static final int OPTION_1 = 1;

    static WindowManager.LayoutParams get(int option) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (option == 1) {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.type = DisplayInfo.getWindowLayoutType();
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.TOP | Gravity.LEFT;
        }

        return params;
    }
}
