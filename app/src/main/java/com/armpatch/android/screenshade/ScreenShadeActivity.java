package com.armpatch.android.screenshade;

import androidx.fragment.app.Fragment;

public class ScreenShadeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ScreenShadeFragment();
    }
}
