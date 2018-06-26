package com.example.canxing.ontimeturnoffscreen.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作TimePeriod对应的数据库表类
 */
public class TimePeriodDB {
    private static final String TAG = "TimePeriodDB";
    private static final String[] TIMPERIODCOLUMNS = {TimePeriod.COLUMN_ID, TimePeriod.COLUMN_START_HOUR, TimePeriod.COLUMN_START_MINUTE,
            TimePeriod.COLUMN_END_MINUTE, TimePeriod.COLUMN_END_HOUR, TimePeriod.COLUMN_IS_ON, TimePeriod.COLUMN_IS_EVERY_DAY,
            TimePeriod.COLUMN_DESCRIPT, TimePeriod.COLUMN_USERNAME};

    private Context context;
    public TimePeriodDB(Context context) {
        this.context = context;
    }

    public TimePeriod getTimeById(int id) {
        String selection = TimePeriod.COLUMN_ID + " = ?";
        String[] args = {String.valueOf(id)};
        List<TimePeriod> timePeriods = getTimes(false, TIMPERIODCOLUMNS, selection,
                args, null, null, null, null);
        if(timePeriods.size() == 0) { return null; }
        else { return timePeriods.get(0); }
    }

    /**
     * 返回TimePeriod数据库表中所有表示打开的时间段
     * @return
     */
    public List<TimePeriod> getItemsIsOn() {
        String selection = TimePeriod.COLUMN_IS_ON + " = ?";
        String[] args = {TimePeriod.ON + ""};
        return getTimes(false, TIMPERIODCOLUMNS, selection, args, null, null, null, null);
    }

    /**
     * 修改一个时间段是否开启的标志
     * @param id 时间段id
     * @param isOn true表示开启，false表示关闭
     */
    public void updateTimePeriodOnById(int id, boolean isOn) {
        ContentValues values = new ContentValues();
        if(isOn) {
            values.put(TimePeriod.COLUMN_IS_ON, TimePeriod.ON);
        } else {
            values.put(TimePeriod.COLUMN_IS_ON, TimePeriod.OFF);
        }
        String where = "id = ?";
        String[] args = {id + ""};
        update(values, where, args);
    }

    private void update(ContentValues values, String where, String[] args) {
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TimePeriod.TABLENAME, values, where, args);
        dbHelper.close();
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
        values.put(TimePeriod.COLUMN_DESCRIPT, fresh.getDescript());
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
        List<TimePeriod> times = getTimes(false, TIMPERIODCOLUMNS, null, null, null, null, null, null);
        return times;
    }
    private List<TimePeriod> getTimes(boolean distinct, String[] column, String selection, String[] args, String groupBy,
                                      String having, String orderBy, String limit) {
        List<TimePeriod> times = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(distinct, TimePeriod.TABLENAME, column, selection, args,
                groupBy, having, orderBy, limit);
        while(cursor.moveToNext()) {
            TimePeriod time = new TimePeriod();
            time.setId(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_ID)));
            time.setStartHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_HOUR)));
            time.setStartMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_MINUTE)));
            time.setEndHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_HOUR)));
            time.setEndMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_MINUTE)));
            time.setIsOn(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_ON)));
            time.setIsEveryDay(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_EVERY_DAY)));
            time.setDescript(cursor.getString(cursor.getColumnIndex(TimePeriod.COLUMN_DESCRIPT)));
            time.setUsername(cursor.getString(cursor.getColumnIndex(TimePeriod.COLUMN_USERNAME)));
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
        values.put(TimePeriod.COLUMN_DESCRIPT, timePeriod.getDescript());
        values.put(TimePeriod.COLUMN_USERNAME, timePeriod.getUsername());
        db.insert(TimePeriod.TABLENAME, null, values);
        dbHelper.close();
    }

    //获取登陆用户的所有时间段
    public List<TimePeriod> getTimesByUsername(String username) {
        String selections = TimePeriod.COLUMN_USERNAME + " = ?";
        String[] args = {username};
        return getTimes(false, TIMPERIODCOLUMNS, selections, args, null, null, null, null);
    }
}
