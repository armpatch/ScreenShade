package com.armpatch.android.screenshade.overlay;

import android.graphics.Point;
import android.view.WindowManager;

public class WindowPosition {

    Overlay overlay;
    WindowManager.LayoutParams params;
    float xPosition;
    float yPosition;

    public WindowPosition(Overlay overlay, WindowManager.LayoutParams layoutParams) {
        params = layoutParams;
        this.overlay = overlay;
    }
//
    public float getXPosition() {
        xPosition = params.x;
        return xPosition;
    }

    public void setXPosition(float xPosition) {
        this.xPosition = xPosition;
        params.x = (int) xPosition;
        overlay.setPositionOnScreen(new Point((int) xPosition,(int) getYPosition()));
    }

    public float getYPosition() {
        yPosition = params.y;
        return yPosition;
    }

    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
        params.y = (int) yPosition;
        overlay.setPositionOnScreen(new Point((int) getXPosition(),(int) yPosition));
    }
}
