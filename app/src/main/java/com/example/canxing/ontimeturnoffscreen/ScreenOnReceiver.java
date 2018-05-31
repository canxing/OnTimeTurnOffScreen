package com.example.canxing.ontimeturnoffscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.util.Calendar;
import java.util.List;

public class ScreenOnReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Calendar now = Calendar.getInstance();
            String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            //nowString = "17:07";
            boolean nowTimeInPeriod = isTimeInPeriod(context, nowString);
            if(nowTimeInPeriod) {
                DevicePolicyUtil.lockNow(context);
                Log.i("screen on receiver", "锁屏啦");
            } else {
                Log.i("screen on receiver", "锁屏失败啦");
            }
        }
    }

    private boolean isTimeInPeriod(Context context, String nowString) {
        TimePeriodDB timePeriodDB = new TimePeriodDB(context);
        List<TimePeriod> times = timePeriodDB.getTimes();
        for(TimePeriod time : times) {
            if(TimeComparing.inPeriod(time.getStartTime(), time.getEndTime(), nowString)) {
                return true;
            }
            Log.i("isTimeInPeriod", nowString);
        }
        return false;
    }

}
