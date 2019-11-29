package com.armpatch.android.screenshade.overlay;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.WindowManager;

abstract class Overlay {

    private WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;

    View windowManagerView;
    DisplayInfo displayInfo;

    boolean isAddedToWindowManager;

    Overlay(Context appContext) {
        windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        displayInfo = new DisplayInfo(appContext);
    }

    void removeViewFromWindowManager() {
        try {
            windowManager.removeView(windowManagerView);
            isAddedToWindowManager = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addViewToWindowManager() {
        try {
            windowManager.addView(windowManagerView, layoutParams);
            isAddedToWindowManager = true;
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    void setPositionOnScreen(Point point) {
        layoutParams.x = point.x;
        layoutParams.y = point.y;

        if (isAddedToWindowManager)
            windowManager.updateViewLayout(windowManagerView, layoutParams);
    }

}
