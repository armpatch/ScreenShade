package com.armpatch.android.secretscreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SecretScreenFragment extends Fragment {

    // constants
    public static final int REQUEST_OVERLAY_PERMISSIONS = 0;

    // variables
    private Context context;

    // ui
    private Button mStartButton;
    private Button mRequestPermissionButtonDrawOver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start_screen, container, false );

        context = inflater.getContext();

        mRequestPermissionButtonDrawOver = v.findViewById(R.id.button_ask_permission_draw_over);
        mRequestPermissionButtonDrawOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivity(i);
            }
        });

        mStartButton = v.findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context != null) {
                    Intent i = WindowHudService.getIntent(getContext());
                    context.startService(i);
                } else {
                    Toast.makeText(getActivity(),"null context", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    //Added this method
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
