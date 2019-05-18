package com.armpatch.android.secretscreen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

public class WindowHudService extends Service {

    // constants
    public static final String BASIC_TAG = WindowHudService.class.getName();

    // variables
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mFocusWindowParams;
    private int displayHeight;
    private int displayWidth;
    private Context context;

    // UI
    private FocusWindowView mFocusWindow;

    public static Intent getIntent(Context context) {
        return new Intent(context, WindowHudService.class);
    }

    // methods
    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplication();
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFocusWindow();

        return START_STICKY;
    }

    @SuppressLint("RtlHardcoded")
    private void showFocusWindow() {
        if (mFocusWindow != null) {
            mWindowManager.removeView(mFocusWindow);
            mFocusWindow = null;
        }

        // Set window type
        int windowType;
        if (Build.VERSION.SDK_INT >= 26) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Set window parameters
        mFocusWindowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //todo: may need to change to hangle touch events
                PixelFormat.TRANSLUCENT);

        // Get Screen Size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        displayHeight = displaymetrics.heightPixels;
        displayWidth = displaymetrics.widthPixels;

        mFocusWindowParams.gravity = Gravity.TOP | Gravity.RIGHT;

        // Retrieve static Drawable and set it's view
        mFocusWindow = new FocusWindowView(context);
        mWindowManager.addView(mFocusWindow, mFocusWindowParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // remove views on destroy!
        if (mFocusWindow != null) {
            mWindowManager.removeView(mFocusWindow);
            mFocusWindow = null;
        }
    }
}
