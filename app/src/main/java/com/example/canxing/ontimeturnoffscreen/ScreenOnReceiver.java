package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.util.Calendar;

public class ScreenOnReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Calendar now = Calendar.getInstance();
            String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            TimePeriod period = getCloseTime(context, nowString);
            if(period != null) {
                DevicePolicyUtil.lockNow(context);
                Log.i("screen on receiver", period.toString());
            } else {
                Log.i("screen on receiver", "null");
            }
        }
    }
    private TimePeriod getCloseTime(Context context, String time) {
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {TimePeriod.COLUMN_START_HOUR, TimePeriod.COLUMN_START_MINUTE, TimePeriod.COLUMN_END_HOUR,
            TimePeriod.COLUMN_END_MINUTE};
        String selection = "(" + TimePeriod.COLUMN_START_HOUR + " < ? and ? < " + TimePeriod.COLUMN_END_HOUR + ") or "
                + "(" + TimePeriod.COLUMN_START_HOUR + " = ? and ? = " + TimePeriod.COLUMN_END_HOUR + " and "
                    + TimePeriod.COLUMN_START_MINUTE + " <= ? and ? <= " + TimePeriod.COLUMN_END_MINUTE + ") or "
                + "(" + TimePeriod.COLUMN_START_HOUR + " = ? and ? < " + TimePeriod.COLUMN_END_HOUR + " and "
                    + TimePeriod.COLUMN_START_MINUTE + " <= ?) or "
                + "(" + TimePeriod.COLUMN_START_HOUR + " < ? and ? = " + TimePeriod.COLUMN_END_HOUR + " and "
                    + " ? <= " + TimePeriod.COLUMN_END_MINUTE + ")";
        String hour = time.split(":")[0];
        String minute = time.split(":")[1];
        String[] args = {hour, hour,
                hour, hour, minute, minute,
                hour, hour, minute,
                hour, hour, minute};
        Cursor cursor =
                db.query(false, TimePeriod.TABLENAME, columns, selection, args, null, null, null, null);
        int min = Integer.MIN_VALUE;
        TimePeriod result = null;
        while(cursor.moveToNext()) {
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setStartHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_HOUR)));
            timePeriod.setStartMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_MINUTE)));
            timePeriod.setEndHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_HOUR)));
            timePeriod.setEndMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_MINUTE)));
            int value = TimeComparing.sub(time, timePeriod.getStartTime());
            Log.i("query", timePeriod.toString());
            if(value > min) {
                min = value;
                result = timePeriod;
            }
        }
        return result;
    }
}
