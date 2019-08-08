package com.armpatch.android.screenshade.overlays;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

class ButtonTouchListener {

    static View.OnTouchListener get () {
        return new View.OnTouchListener() {

            Point firstDown = new Point();
            Point location = new Point();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                VelocityTracker tracker = VelocityTracker.obtain();

                event.setLocation(event.getRawX(), event.getRawY());
                tracker.addMovement(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        firstDown.set((int) event.getX(), (int) event.getY());
                        //location.set(layoutParams.x, layoutParams.y);
                    }

                    case MotionEvent.ACTION_MOVE: {
                        int movementX = (int) event.getX() - firstDown.x;
                        int movementY = (int) event.getY() - firstDown.y;

                        Point newPosition = new Point();
                        newPosition.set(
                                location.x + movementX,
                                location.y + movementY
                        );
                        //updatePosition(newPosition);
                    }

                    case MotionEvent.ACTION_UP: {
                        tracker.computeCurrentVelocity(1000);
                        float velocityX = tracker.getXVelocity();
                        float volocityY = tracker.getYVelocity();
                    }

                    tracker.recycle();
                }
                return false;
            }
        };
    }

}
