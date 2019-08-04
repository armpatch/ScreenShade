package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;
import android.view.View;

public class CoordinateFinder {

    static Point getCenterPoint(View v, Point topLeftPoint) {
        int width = v.getWidth();
        int height = v.getHeight();

        Point centerPoint = new Point();

        centerPoint.x = topLeftPoint.x + width/2;
        centerPoint.y = topLeftPoint.y + height/2;

        return centerPoint;
    }
}
