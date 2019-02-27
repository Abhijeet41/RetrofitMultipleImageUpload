package com.wave.fileuploadservice.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wave.fileuploadservice.FileUploadService;

import java.util.Objects;

public class RetryJobReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent mIntent = new Intent(context, FileUploadService.class);
            //intent.putExtra("sleepTime", 12);
            FileUploadService.enqueueWork(context, mIntent);
        }
        private static final String TAG = "NotificationActionRecei";
        private static final String ACTION_RETRY = "com.leher.ACTION_RETRY";
        private static final String ACTION_CLEAR = "com.leher.ACTION_CLEAR";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            int notificationId = intent.getIntExtra("notificationId", 0);
            int rowId = intent.getIntExtra("mRowId", 0);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_RETRY:
                    Bugfender.d(LoggingState.TAG, LoggingState.VIDEO_RETRY.value() + NetworkUtils.isConnectedFast(LeherApp.getContext()));
                    manager.cancel(notificationId);
                    EventBus.getDefault().post(new OnReceiverEvent(rowId));
                    break;
                case ACTION_CLEAR:
                    Bugfender.d(LoggingState.TAG, LoggingState.VIDEO_CANCEL.value() + NetworkUtils.isConnectedFast(LeherApp.getContext()));
                    manager.cancel(notificationId);
                    break;
                default:
                    break;
            }


        }
    }
}