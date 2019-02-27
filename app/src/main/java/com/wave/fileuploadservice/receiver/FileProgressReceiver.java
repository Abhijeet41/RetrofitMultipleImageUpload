package com.wave.fileuploadservice.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.leher.LeherApp;
import com.leher.R;
import com.leher.app.Constants;
import com.leher.helper.NotificationHelper;
import com.leher.ui.main.MainActivity;

import java.util.Objects;

public class FileProgressReceiver extends BroadcastReceiver {
    private static final String TAG = "FileProgressReceiver";
    public static final String ACTION_CLEAR_NOTIFICATION = "com.leher.ACTION_CLEAR_NOTIFICATION";
    public static final String ACTION_PROGRESS_NOTIFICATION = "com.leher.ACTION_PROGRESS_NOTIFICATION";
    public static final String ACTION_UPLOADED = "com.leher.ACTION_UPLOADED";
    private NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    NotificationHelper mHelper;

    @Override
    public void onReceive(Context mContext, Intent intent) {
        // an Intent broadcast.
        Log.d(TAG, "onReceive: ");
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mHelper =new NotificationHelper(mContext);
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        int notificationId = intent.getIntExtra("notificationId", 1);
        int progress = intent.getIntExtra("progress", 0);


        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_PROGRESS_NOTIFICATION:

                Log.d(TAG, "onReceive: PROGRESS" + ACTION_PROGRESS_NOTIFICATION);
                mBuilder = new NotificationCompat.Builder(mContext, Constants.NOTIFICATION_CHANNEL_ID);
                mBuilder.setSmallIcon(R.drawable.ic_stat_notification);
                mBuilder.setColor(ContextCompat.getColor(mContext, R.color.windows_blue));
                mBuilder.setContentTitle(mContext.getString(R.string.uploading))
                        .setContentText(mContext.getString(R.string.in_progress))
                        //  .setOngoing(true)
                        .setContentIntent(resultPendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

                mBuilder.setVibrate(new long[]{0L});
                mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.NOTIFICATION_CHANNEL_NAME, importance);
                    notificationChannel.enableLights(false);
                    notificationChannel.enableVibration(false);
                    mBuilder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                mBuilder.setProgress(100, progress, false);
                mNotificationManager.notify(notificationId, mBuilder.build());


                break;
            case ACTION_CLEAR_NOTIFICATION:

                Log.d(TAG, "onReceive: " + ACTION_CLEAR_NOTIFICATION);
                NotificationManager nManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.cancel(notificationId);


                break;
            case ACTION_UPLOADED:


                String mWaveId = intent.getStringExtra("wave_id");
                Intent waveIntent = new Intent(LeherApp.getInstance(), MainActivity.class);
                waveIntent.putExtra(MainActivity.LINK_TYPE, "WAVE");
                waveIntent.putExtra(MainActivity.LINK_ID, mWaveId);
                mHelper.updateNotification(mContext.getString(R.string.message_opinion_uploading), "", waveIntent, notificationId);


                break;
            default:
                break;
        }
    }
}
