package com.armpatch.android.screenshade.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.armpatch.android.screenshade.fragment.SingleFragmentActivity;
import com.armpatch.android.screenshade.fragment.StartScreenFragment;

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
