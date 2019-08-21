package com.armpatch.android.screenshade.overlay;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

class DisplayInfo {

    private Context context;

    private int screenHeight;
    private int screenWidth;
    private int diagonalLength;
    private int navBarHeight;
    private int statusBarHeight;

    // non-private methods

    DisplayInfo(Context context) {
        this.context = context;
        update();
    }

    private void update() {
        setScreenHeight();
        screenWidth = getDisplayMetrics().widthPixels;
        diagonalLength = (int) Math.sqrt(Math.pow(screenHeight,2)+ Math.pow(screenWidth,2));
        setNavBarHeight();
        setStatusBarHeight();
    }

    int getScreenHeight() {
        return screenHeight;
    }

    int getScreenWidth() {
        return screenWidth;
    }

    int getScreenDiagonal() {
        return diagonalLength;
    }

    int getNavBarHeight() {
        return navBarHeight;
    }

    int getStatusBarHeight() {
        return statusBarHeight;
    }

    static Point getCenterShiftedPoint(View v, Point centerPoint) {
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        Point shiftedPoint = new Point();

        shiftedPoint.x = centerPoint.x - width/2;
        shiftedPoint.y = centerPoint.y - height/2;

        return shiftedPoint;
    }

    public static int convertDpToPixel (float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static int convertPixelToDp (float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = 160f * px / metrics.densityDpi;
        return Math.round(dp);
    }

    private void setStatusBarHeight() {
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        } else { statusBarHeight = 0; }
    }

    private void setScreenHeight() {
        screenHeight = getDisplayMetrics().heightPixels;
    }

    private void setNavBarHeight() {
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

    private DisplayMetrics getDisplayMetrics () {
        WindowManager wManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics dMetrics = new DisplayMetrics();

        wManager.getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics;
    }
}