package com.ketanchoyal.crossfire;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class crossfiremessagingservice extends FirebaseMessagingService {

    public static final String ANDROID_CHANNEL_ID = "com.ketanchoyal.crossfire_TARGET_NOTIFICATION";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title=remoteMessage.getNotification().getTitle();
        String notification_message=remoteMessage.getNotification().getBody();

        String click_action=remoteMessage.getNotification().getClickAction();

        String from_user_id=remoteMessage.getData().get("from_user_id");
        String click = remoteMessage.getData().get("click_action");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);
        //startActivity(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int)System.currentTimeMillis();
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
