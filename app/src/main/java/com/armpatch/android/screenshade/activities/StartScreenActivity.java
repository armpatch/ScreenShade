package com.armpatch.android.screenshade.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.armpatch.android.screenshade.fragments.ScreenShadeFragment;
import com.armpatch.android.screenshade.fragments.SingleFragmentActivity;

public class StartScreenActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ScreenShadeFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context applicationContext = getApplicationContext();
    }
}
