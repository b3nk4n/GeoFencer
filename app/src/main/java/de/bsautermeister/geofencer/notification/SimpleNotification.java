package de.bsautermeister.geofencer.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import de.bsautermeister.geofencer.R;

public class SimpleNotification {

    private static final String CHANNEL_ID = "de.bsautermeister.geofencer";

    private final Context context;
    private final NotificationManager notificationManager;

    public SimpleNotification(Context appContext) {
        this.context = appContext;
        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel(CHANNEL_ID, "Geofencer", "Geofencing notifications.");
    }

    private void createChannel(String id, String name, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name,
                    NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public void show(int id, String content) {
        Notification.Builder nb;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb  = new Notification.Builder(context, CHANNEL_ID);
        } else {
            nb  = new Notification.Builder(context);
        }
        Notification notification = nb.setContentTitle("Geofencer")
                                        .setContentText(content)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setAutoCancel(true).build();

        notificationManager.notify(id, notification);
    }
}
