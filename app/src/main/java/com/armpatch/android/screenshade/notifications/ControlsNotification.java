package com.armpatch.android.screenshade.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.armpatch.android.screenshade.R;
import com.armpatch.android.screenshade.activities.StartScreenActivity;
import com.armpatch.android.screenshade.services.OverlayService;

public class ControlsNotification {

    private static final String CHANNEL_ID = "channel id";
    private final Context context;
    private final NotificationCompat.Builder builder;


    public ControlsNotification(Context context) {
        this.context = context;
        builder = getBuilder();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null,null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder getBuilder() {
        Resources resources = context.getResources();


        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.window_shade_96)
                .setContentTitle(resources.getString(R.string.notification_title))
                .setContentText(resources.getString(R.string.notification_description))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.button))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntentForService())
                .setAutoCancel(true);
    }

    private PendingIntent getPendingIntentForActivity() {
        Intent intent = new Intent(context, StartScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private PendingIntent getPendingIntentForService() {
        Intent intent = new Intent(context, OverlayService.class);
        return PendingIntent.getService(context, 0, intent, 0);
    }


    public void sendNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
    }

}
