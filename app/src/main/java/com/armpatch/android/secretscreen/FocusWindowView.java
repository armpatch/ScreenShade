package com.armpatch.android.secretscreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class FocusWindowView extends View {

    private static final String TAG = "FocusWindowView";

    private Box mViewBox;

    // draw variables
    private Paint mBackgroundPaint;
    private Paint mBoxPaint;

    //
    private static float boxStrokeWidth = 3;

    public FocusWindowView(Context context) {
        super(context);
        init(null);
    }

    public FocusWindowView(Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray typedArray =
                getContext().obtainStyledAttributes(attrs, R.styleable.FocusWindowView);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(typedArray.
                getColor(R.styleable.FocusWindowView_fill_color, Color.TRANSPARENT));

        typedArray.recycle();

        mBoxPaint = new Paint();

        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(boxStrokeWidth);
        mBoxPaint.setColor(Color.BLACK);

        if (attrs == null)
            return;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawPaint(mBackgroundPaint);

        if (mViewBox != null) {
            float left = Math.min(mViewBox.getOrigin().x, mViewBox.getCurrent().x);
            float right = Math.max(mViewBox.getOrigin().x, mViewBox.getCurrent().x);
            float top = Math.min(mViewBox.getOrigin().y, mViewBox.getCurrent().y);
            float bottom = Math.max(mViewBox.getOrigin().y, mViewBox.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mViewBox = new Box(current);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mViewBox != null) {
                    mViewBox.setCurrent(current);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mViewBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCELL";
                mViewBox = null;
                break;
        }

        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);

        return true;
    }
}

