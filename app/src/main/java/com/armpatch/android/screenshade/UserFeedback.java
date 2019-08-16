package com.armpatch.android.screenshade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class UserFeedback {


    public static void sendEmailFeedback(Context appContext) {
        final String[] myEmail = {"aaronpatch.developer@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, myEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScreenShade - Feedback");

        if (emailIntent.resolveActivity(appContext.getPackageManager()) != null) {
            appContext.startActivity(emailIntent);
        }
    }
}
