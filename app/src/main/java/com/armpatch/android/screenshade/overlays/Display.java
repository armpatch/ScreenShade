package com.armpatch.android.screenshade.overlays;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

class Display {


    static int getHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    static int getWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    public static int getDiagonal(Context context) {
        int height = getHeight(context);
        int width = getWidth(context);

        // pythagorean theorem
        return (int) Math.sqrt( (height * height) + (width * width) );
    }

    static int getNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else {
            Log.i("TAG", "NavBarHeight returned 0");
            return 0;
        }
    }

    private static DisplayMetrics getDisplayMetrics (Context context) {
        WindowManager wManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics dMetrics = new DisplayMetrics();

        wManager.getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics;
    }
}