package com.example.canxing.ontimeturnoffscreen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * 用于注册广播的服务
 */
public class RegisterReceiverService extends Service {
    private static final String TAG = "REGISTERRECEIVERSERVIE";
    private ScreenOnReceiver receiver;
    public RegisterReceiverService() {
    }

    @Override
    public void onCreate() {
        IntentFilter screenFilte = new IntentFilter(Intent.ACTION_SCREEN_ON);
        receiver = new ScreenOnReceiver();
        registerReceiver(receiver, screenFilte);
        super.onCreate();
    }
    public static final String CHANNEIId = "SCREEN";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand", "onStartCommand()");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEIId);
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEIId, "System", NotificationManager.IMPORTANCE_LOW);
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setShowWhen(true);
        builder.setContentTitle("foreground service");
        startForeground(0x0001, builder.build());
        Log.i("RegisterReceiverService", "end....");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
