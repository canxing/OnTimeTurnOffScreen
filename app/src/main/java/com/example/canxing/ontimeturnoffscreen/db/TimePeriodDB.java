package com.example.canxing.ontimeturnoffscreen.db;

import android.content.ContentValues;
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
    private Context context;
    public TimePeriodDB(Context context) {
        this.context = context;
    }

    /**
     * 根据时间段的id删除在数据库表中删除一个时间段
     * @param id
     */
    public void deleteById(int id) {
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = "id = ?";
        String[] args = {String.valueOf(id)};
        db.delete(TimePeriod.TABLENAME, where, args);
        dbHelper.close();
    }

    /**
     * 更新一个时间段
     * @param old
     * @param fresh
     */
    public void update(TimePeriod old, TimePeriod fresh) {
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TimePeriod.COLUMN_START_HOUR, fresh.getStartHour());
        values.put(TimePeriod.COLUMN_START_MINUTE, fresh.getStartMinute());
        values.put(TimePeriod.COLUMN_END_HOUR, fresh.getEndHour());
        values.put(TimePeriod.COLUMN_END_MINUTE, fresh.getEndMinute());
        values.put(TimePeriod.COLUMN_IS_EVERY_DAY, fresh.getIsEveryDay());
        String where = TimePeriod.COLUMN_START_HOUR + " = ? and "
                + TimePeriod.COLUMN_START_MINUTE + " = ? and "
                + TimePeriod.COLUMN_END_HOUR + " = ? and "
                + TimePeriod.COLUMN_END_MINUTE + " = ? and "
                + TimePeriod.COLUMN_IS_EVERY_DAY + " = ?"
                ;
        String[] args = {String.valueOf(old.getStartHour()), String.valueOf(old.getStartMinute()),
                String.valueOf(old.getEndHour()), String.valueOf(old.getEndMinute()), String.valueOf(old.getIsEveryDay())};
        db.update(TimePeriod.TABLENAME, values, where, args);
        dbHelper.close();
    }

    /**
     * 获取所有时间段对象
     * @return
     */
    public List<TimePeriod> getTimes() {
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

    /**
     * 将一个时间段对象保存到数据库表中
     * @param timePeriod
     */
    public void insert(TimePeriod timePeriod) {
        DBHelper dbHelper = new DBHelper(this.context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TimePeriod.COLUMN_START_HOUR, timePeriod.getStartHour());
        values.put(TimePeriod.COLUMN_START_MINUTE, timePeriod.getStartMinute());
        values.put(TimePeriod.COLUMN_END_HOUR, timePeriod.getEndHour());
        values.put(TimePeriod.COLUMN_END_MINUTE, timePeriod.getEndMinute());
        values.put(TimePeriod.COLUMN_IS_ON, timePeriod.getIsOn());
        values.put(TimePeriod.COLUMN_IS_EVERY_DAY, timePeriod.getIsEveryDay());
        db.insert(TimePeriod.TABLENAME, null, values);
        dbHelper.close();
    }
}
