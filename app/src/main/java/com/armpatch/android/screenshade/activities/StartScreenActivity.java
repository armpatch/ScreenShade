package com.armpatch.android.screenshade.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.armpatch.android.screenshade.fragments.SingleFragmentActivity;
import com.armpatch.android.screenshade.fragments.StartScreenFragment;

public class StartScreenActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new StartScreenFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
