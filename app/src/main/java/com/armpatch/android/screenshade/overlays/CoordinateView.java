package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;
import android.view.View;

public class CoordinateView {

    private View view;

    public CoordinateView (View v) {
        view = v;
    }

    static Point getCenterPoint(View v, Point topLeftPoint) {
        int width = v.getWidth();
        int height = v.getHeight();

        Point centerPoint = new Point();

        centerPoint.x = topLeftPoint.x + width/2;
        centerPoint.y = topLeftPoint.y + height/2;

        return centerPoint;
    }

    public Point getCenterShiftedPoint(Point topLeftPoint) {
        int width = view.getWidth();
        int height = view.getHeight();

        Point shiftedPoint = new Point();

        shiftedPoint.x = topLeftPoint.x - width/2;
        shiftedPoint.y = topLeftPoint.y - height/2;

        return shiftedPoint;
    }
}
