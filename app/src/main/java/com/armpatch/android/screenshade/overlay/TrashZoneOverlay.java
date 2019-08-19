package com.armpatch.android.screenshade.overlay;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.armpatch.android.screenshade.R;


public class TrashZoneOverlay extends Overlay {


    public TrashZoneOverlay(Context appContext) {
        super(appContext);

        DisplayInfo displayInfo = new DisplayInfo(appContext);

        windowManagerView = View.inflate(appContext, R.layout.trash_zone_overlay, null);
        windowManagerView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));

        layoutParams = WindowLayoutParams.getDefaultParams();
        layoutParams.width = displayInfo.getWidth();
        layoutParams.y = displayInfo.getNavBarHeight();
    }

    void show() {
        addViewToWindowManager();
    }

    void hide() {
        removeViewFromWindowManager();
    }
}
