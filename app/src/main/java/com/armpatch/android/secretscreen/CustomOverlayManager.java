package com.armpatch.android.secretscreen;

import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

public class CustomOverlayManager {

    Context baseContext;
    static int windowLayoutType;
    WindowManager windowManager;


    static {
        if (Build.VERSION.SDK_INT >= 26) {
            windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowLayoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }


    public CustomOverlayManager(Context context) {
        baseContext = context;
        windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
    }

}
