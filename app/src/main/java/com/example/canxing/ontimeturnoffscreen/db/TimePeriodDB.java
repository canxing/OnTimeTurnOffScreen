package com.example.canxing.ontimeturnoffscreen.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作TimePeriod对应的数据库表类
 */
public class TimePeriodDB {
    private static final String TAG = "TimePeriodDB";
    public static List<TimePeriod> getTimes(Context context) {
        List<TimePeriod> times = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {TimePeriod.COLUMN_ID, TimePeriod.COLUMN_START_HOUR, TimePeriod.COLUMN_START_MINUTE,
                TimePeriod.COLUMN_END_MINUTE, TimePeriod.COLUMN_END_HOUR, TimePeriod.COLUMN_IS_ON, TimePeriod.COLUMN_IS_EVERY_DAY};
        Cursor cursor = db.query(true, TimePeriod.TABLENAME, columns, null, null, null, null, null, null);
        while(cursor.moveToNext()) {
            TimePeriod time = new TimePeriod();
            time.setId(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_ID)));
            time.setStartHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_HOUR)));
            time.setStartMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_MINUTE)));
            time.setEndHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_HOUR)));
            time.setEndMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_MINUTE)));
            time.setIsOn(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_ON)));
            time.setIsEveryDay(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_EVERY_DAY)));
            times.add(time);
            Log.i(TAG, time.toString());
        }
        dbHelper.close();
        return times;
    }
}
