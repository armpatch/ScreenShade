package com.armpatch.android.secretscreen;

import androidx.fragment.app.Fragment;

public class SecretScreenActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SecretScreenFragment();
    }
}
