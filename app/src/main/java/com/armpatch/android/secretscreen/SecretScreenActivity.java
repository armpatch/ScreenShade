package com.armpatch.android.secretscreen;

import android.support.v4.app.Fragment;

public class SecretScreenActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SecretScreenFragment();
    }
}
