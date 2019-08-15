package com.armpatch.android.screenshade.overlays;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.armpatch.android.screenshade.services.OverlayService;

class Display {

    static int height;
    static int width;
    static int navigationBarHeight;

    public Display(Context activity) {
        setHeightField(activity);
        setWidthField(activity);
        setNavBarHeight(activity);
    }

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static int getNavBarHeight() {
        return navigationBarHeight;
    }

    public static int getDiagonal() {
        // pythagorean theorem
        return (int) Math.sqrt( (height * height) + (width * width) );
    }


    static Point getCenterShiftedPoint(View v, Point centerPoint) {
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        Point shiftedPoint = new Point();

        shiftedPoint.x = centerPoint.x - width/2;
        shiftedPoint.y = centerPoint.y - height/2;

        return shiftedPoint;
    }

    static boolean isInTrashZone(OverlayService service, Point point) {
        int zoneHeight = 400;
        return height - zoneHeight < point.y;
    }

    static void moveIntoScreenBounds(View buttonContainer, OverlayService service, Point originalPoint) {
        int Y_MIN = 0;
        int Y_MAX = height - buttonContainer.getLayoutParams().height;
        int X_MIN = 0;
        int X_MAX = height - buttonContainer.getLayoutParams().width;

        if (originalPoint.x < X_MIN)
            originalPoint.x = X_MIN;

        if (X_MAX < originalPoint.x)
            originalPoint.x = X_MAX;

        if (originalPoint.y < Y_MIN)
            originalPoint.y = Y_MIN;

        if (Y_MAX < originalPoint.y)
            originalPoint.y = Y_MAX;
    }

    // private methods

    private static DisplayMetrics getDisplayMetrics (Context context) {
        WindowManager wManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        DisplayMetrics dMetrics = new DisplayMetrics();

        wManager.getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics;
    }

    private static void setHeightField(Context context) {
        height = getDisplayMetrics(context).heightPixels;
    }

    private static void setWidthField(Context context) {
        width = getDisplayMetrics(context).widthPixels;
    }

    private static void setNavBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");

        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            Log.i("TAG", "NavBarHeight returned 0");
            navigationBarHeight = 0;
        }
    }
}