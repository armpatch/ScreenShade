package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;
import android.view.View;

class CoordinateMaker {

    static Point getCenterPoint(View v, Point topLeftPoint) {
        int width = v.getWidth();
        int height = v.getHeight();

        Point centerPoint = new Point();

        centerPoint.x = topLeftPoint.x + width/2;
        centerPoint.y = topLeftPoint.y + height/2;

        return centerPoint;
    }

    /**
     * @param v
     * @param centerPoint
     * @return a Point above and left of the centerPoint, which when used to position the view
     * will center it on the centerPoint
     */
    static Point getCenterShiftedPoint(View v, Point centerPoint) {
        int width = v.getLayoutParams().width;
        int height = v.getLayoutParams().height;

        Point shiftedPoint = new Point();

        shiftedPoint.x = centerPoint.x - width/2;
        shiftedPoint.y = centerPoint.y - height/2;

        return shiftedPoint;
    }
}
