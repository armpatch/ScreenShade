package com.armpatch.android.screenshade.overlays;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class Display {

    private static int screenHeight;
    private static int screenWidth;
    private static int diagonalLength;
    private static int navBarHeight;

    // public methods

    public Display(Context context) {
        screenHeight = getDisplayMetrics(context).heightPixels;
        screenWidth = getDisplayMetrics(context).widthPixels;
        diagonalLength = (int) Math.sqrt(Math.pow(screenHeight,2)+ Math.pow(screenWidth,2));
        setNavBarHeight(context);
    }

    static int getHeight() {
        return screenHeight;
    }

    static int getWidth() {
        return screenWidth;
    }

    static int getDiagonal() {
        return diagonalLength;
    }

    static int getNavBarHeight() {
        return navBarHeight;
    }

    // private methods

    private static void setNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");

        if (resourceId > 0) {
            navBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            Log.i("TAG", "NavBarHeight returned 0");
            navBarHeight = 0;
        }
    }

    private static DisplayMetrics getDisplayMetrics (Context context) {
        WindowManager wManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics dMetrics = new DisplayMetrics();

        wManager.getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics;
    }
}