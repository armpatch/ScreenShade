package com.armpatch.android.screenshade.activities;

import androidx.fragment.app.Fragment;

import com.armpatch.android.screenshade.fragments.ScreenShadeFragment;
import com.armpatch.android.screenshade.fragments.SingleFragmentActivity;

public class ScreenShadeActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ScreenShadeFragment();
    }
}
