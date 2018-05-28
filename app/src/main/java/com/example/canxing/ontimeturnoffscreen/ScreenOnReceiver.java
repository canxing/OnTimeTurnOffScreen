package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.util.Calendar;

public class ScreenOnReceiver extends BroadcastReceiver {
    private DevicePolicyManager  mDPM;
    private ComponentName mDeviceAdminSample;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_ON)){
            if(mDPM == null) {
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDeviceAdminSample = new ComponentName(context, ScreenOffAdminReceiver.class);
            }
            Calendar now = Calendar.getInstance();
            String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            String other = "17:20";
            if(TimeComparing.isBefore(nowString, other)) {
                mDPM.lockNow();
            }
            Log.i("screen on receiver", "screen on");
        }
    }
}
