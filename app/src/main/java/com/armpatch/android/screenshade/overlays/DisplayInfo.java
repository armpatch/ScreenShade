package com.armpatch.android.screenshade.overlays;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class DisplayInfo {

    private DisplayInfo displayInfo;

    int displayHeight;
    int navigationBarHeight;
    int statusBarHeight;
    Context appContext;

    public DisplayInfo get(Context applicationContext) {
        if (displayInfo == null) {
            displayInfo = new DisplayInfo(applicationContext);
        }
        return displayInfo;
    }

    private DisplayInfo(Context applicationContext) {
        appContext = applicationContext;
    }

    public int getDisplayHeight() {
        WindowManager wManager = (WindowManager) appContext.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics dMetrics = new DisplayMetrics();

        wManager.getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics.heightPixels;
    }

    public int getNavBarHeight() {
        Resources resources = appContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else {
            Log.i("TAG", "NavBarHeight returned 0");
            return 0;
        }
    }
}
