package com.wave.fileuploadservice.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.wave.fileuploadservice.MainActivity;
import com.wave.fileuploadservice.NotificationHelper;

import java.util.Objects;

public class FileProgressReceiver extends BroadcastReceiver {
    private static final String TAG = "FileProgressReceiver";
    public static final String ACTION_CLEAR_NOTIFICATION = "com.wave.ACTION_CLEAR_NOTIFICATION";
    public static final String ACTION_PROGRESS_NOTIFICATION = "com.wave.ACTION_PROGRESS_NOTIFICATION";
    public static final String ACTION_UPLOADED = "com.wave.ACTION_UPLOADED";

    NotificationHelper mNotificationHelper;
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder notification;

    @Override
    public void onReceive(Context mContext, Intent intent) {
        mNotificationHelper = new NotificationHelper(mContext);
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        int notificationId = intent.getIntExtra("notificationId", 1);
        int progress = intent.getIntExtra("progress", 0);


        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_PROGRESS_NOTIFICATION:
                notification = mNotificationHelper.getNotification("", "", progress);
                mNotificationHelper.notify(NOTIFICATION_ID, notification);
                break;
            case ACTION_CLEAR_NOTIFICATION:
                mNotificationHelper.cancelNotification(notificationId);
                break;
            case ACTION_UPLOADED:
                notification = mNotificationHelper.getNotification("", "", resultPendingIntent);
                mNotificationHelper.notify(NOTIFICATION_ID, notification);
//                String mWaveId = intent.getStringExtra("wave_id");
//                Intent waveIntent = new Intent(LeherApp.getInstance(), MainActivity.class);
//                waveIntent.putExtra(MainActivity.LINK_TYPE, "WAVE");
//                waveIntent.putExtra(MainActivity.LINK_ID, mWaveId);
//                mHelper.updateNotification(mContext.getString(R.string.message_opinion_uploading), "", waveIntent, notificationId);


                break;
            default:
                break;
        }

    }
}
