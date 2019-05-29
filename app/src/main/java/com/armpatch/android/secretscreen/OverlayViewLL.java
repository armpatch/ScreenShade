package com.armpatch.android.secretscreen;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class OverlayViewLL extends LinearLayout {

    static int windowLayoutType;

    float lastTouchY;
    float PosY;

    WindowManager.LayoutParams layoutParams;

    public OverlayViewLL(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowLayoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);





    }


}
