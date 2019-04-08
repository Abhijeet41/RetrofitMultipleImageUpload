package com.wave.fileuploadservice;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.wave.fileuploadservice.receiver.RetryJobReceiver;
import com.wave.fileuploadservice.service.CountingRequestBody;
import com.wave.fileuploadservice.service.RestApiService;
import com.wave.fileuploadservice.service.RetrofitInstance;
import com.wave.fileuploadservice.utils.MIMEType;

import java.io.File;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.wave.fileuploadservice.receiver.RetryJobReceiver.ACTION_CLEAR;
import static com.wave.fileuploadservice.receiver.RetryJobReceiver.ACTION_RETRY;

public class FileUploadService extends JobIntentService {
    private static final String TAG = "FileUploadService";
    RestApiService apiService;
    Disposable mDisposable;
    public static final int NOTIFICATION_ID = 1;
    public static final int NOTIFICATION_RETRY_ID = 2;
    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 102;
    String mFilePath;

    NotificationHelper mNotificationHelper;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FileUploadService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        /**
         * Download/Upload of file
         * The system or framework is already holding a wake lock for us at this point
         */

        // get file file here
        mFilePath = intent.getStringExtra("mFilePath");
        if (mFilePath == null) {
            Log.e(TAG, "onHandleWork: Invalid file URI");
            return;
        }
        apiService = RetrofitInstance.getApiService();
        Flowable<Double> fileObservable = Flowable.create(new FlowableOnSubscribe<Double>() {
            @Override
            public void subscribe(FlowableEmitter<Double> emitter) throws Exception {
                apiService.onFileUpload(FileUploadService.this.createRequestBodyFromText("info@androidwave.com"), FileUploadService.this.createMultipartBody(mFilePath, emitter)).blockingGet();
                emitter.onComplete();
            }
        }, BackpressureStrategy.LATEST);

        mDisposable = fileObservable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Double>() {
                    @Override
                    public void accept(Double progress) throws Exception {
                        FileUploadService.this.onProgress(progress);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        FileUploadService.this.onErrors(throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        FileUploadService.this.onSuccess();
                    }
                });
    }

    private void onErrors(Throwable throwable) {
        Intent successIntent = new Intent("com.wave.ACTION_CLEAR_NOTIFICATION");
        successIntent.putExtra("notificationId", NOTIFICATION_ID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);



        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, null,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent retryIntent = new Intent(this, RetryJobReceiver.class);
        retryIntent.putExtra("notificationId", NOTIFICATION_RETRY_ID);
        retryIntent.putExtra("mFilePath", mFilePath);
        retryIntent.setAction(ACTION_RETRY);

        Intent clearIntent = new Intent(this, RetryJobReceiver.class);
        clearIntent.putExtra("notificationId", NOTIFICATION_RETRY_ID);
        clearIntent.putExtra("mFilePath", mFilePath);
        clearIntent.setAction(ACTION_CLEAR);

        PendingIntent retryPendingIntent = PendingIntent.getBroadcast(this, 0, retryIntent, 0);
        PendingIntent clearPendingIntent = PendingIntent.getBroadcast(this, 0, clearIntent, 0);
        NotificationCompat.Builder mBuilder = mNotificationHelper.getNotification("", "", resultPendingIntent);

        mBuilder.addAction(android.R.drawable.ic_menu_revert, getString(R.string.btn_retry_not), retryPendingIntent);
        mBuilder.addAction(android.R.drawable.ic_menu_revert, getString(R.string.btn_cancel_not), clearPendingIntent);
        mNotificationHelper.notify(NOTIFICATION_RETRY_ID, mBuilder);
    }

    private void onProgress(Double progress) {
        Intent progressIntent = new Intent("com.wave.ACTION_PROGRESS_NOTIFICATION");
        progressIntent.putExtra("notificationId", NOTIFICATION_ID);
        progressIntent.putExtra("progress", (int) (100 * progress));
        LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);
    }

    private void onSuccess() {
        sendBroadcastMeaasge("File uploading successful ");
        Intent successIntent = new Intent("com.wave.ACTION_UPLOADED");
        successIntent.putExtra("notificationId", NOTIFICATION_ID);
        successIntent.putExtra("progress", 100);
        LocalBroadcastManager.getInstance(this).sendBroadcast(successIntent);
    }

    public void sendBroadcastMeaasge(String message) {
        Intent localIntent = new Intent("my.own.broadcast");
        localIntent.putExtra("result", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private RequestBody createRequestBodyFromFile(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    private RequestBody createRequestBodyFromText(String mText) {
        return RequestBody.create(MediaType.parse("text/plain"), mText);
    }


    /**
     * return multi part body in format of FlowableEmitter
     *
     * @param filePath
     * @param emitter
     * @return
     */
    private MultipartBody.Part createMultipartBody(String filePath, FlowableEmitter<Double> emitter) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("myFile", file.getName(), createCountingRequestBody(file, MIMEType.IMAGE.value, emitter));
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, final FlowableEmitter<Double> emitter) {
        RequestBody requestBody = createRequestBodyFromFile(file, mimeType);
        return new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesWritten, long contentLength) {
                double progress = (1.0 * bytesWritten) / contentLength;
                emitter.onNext(progress);
            }
        });
    }
}
