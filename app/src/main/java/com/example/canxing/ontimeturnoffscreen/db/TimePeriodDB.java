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
    //查询语句的列名称
    private static final String[] TIMPERIODCOLUMNS = {TimePeriod.COLUMN_ID, TimePeriod.COLUMN_START_HOUR, TimePeriod.COLUMN_START_MINUTE,
            TimePeriod.COLUMN_END_MINUTE, TimePeriod.COLUMN_END_HOUR, TimePeriod.COLUMN_IS_ON, TimePeriod.COLUMN_IS_EVERY_DAY,
            TimePeriod.COLUMN_DESCRIPT, TimePeriod.COLUMN_USERNAME};

    private Context context;
    public TimePeriodDB(Context context) {
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    //  查询
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * 根据时间段id返回对应的时间段对象
     * @param id 时间段id
     * @return 对应时间段的对象，如果没有，返回null
     */
    public TimePeriod getTimeById(int id) {
        String selection = TimePeriod.COLUMN_ID + " = ?";
        String[] args = {String.valueOf(id)};
        //查询对应id的时间段
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
    //获取登陆用户的所有时间段
    public List<TimePeriod> getTimesByUsername(String username) {
        String selections = TimePeriod.COLUMN_USERNAME + " = ?";
        String[] args = {username};
        return getTimes(false, TIMPERIODCOLUMNS, selections, args, null, null, null, null);
    }
    /**
     * 获取所有时间段对象
     * @return
     */
    public List<TimePeriod> getTimes() {
        List<TimePeriod> times = getTimes(false, TIMPERIODCOLUMNS, null, null, null, null, null, null);
        return times;
    }

    //查询操作的基本操作，所有查询操作都可以透过这个方法进行查询
    private List<TimePeriod> getTimes(boolean distinct, String[] column, String selection, String[] args, String groupBy,
                                      String having, String orderBy, String limit) {
        List<TimePeriod> times = new ArrayList<>();
        //1. 获取数据连接
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        //2. 打开只读数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //3. 数据库查询
        Cursor cursor = db.query(distinct, TimePeriod.TABLENAME, column, selection, args,
                groupBy, having, orderBy, limit);
        //4. 返回值
        //4.1 迭代游标,每一次moveToNext游标移动到下一条查询结果
        while(cursor.moveToNext()) {
            TimePeriod time = new TimePeriod();
            //4.2 获取每一条数据对应的值，并且保存在TimePeriod对象中
            time.setId(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_ID)));
            time.setStartHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_HOUR)));
            time.setStartMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_START_MINUTE)));
            time.setEndHour(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_HOUR)));
            time.setEndMinute(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_END_MINUTE)));
            time.setIsOn(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_ON)));
            time.setIsEveryDay(cursor.getInt(cursor.getColumnIndex(TimePeriod.COLUMN_IS_EVERY_DAY)));
            time.setDescript(cursor.getString(cursor.getColumnIndex(TimePeriod.COLUMN_DESCRIPT)));
            time.setUsername(cursor.getString(cursor.getColumnIndex(TimePeriod.COLUMN_USERNAME)));
            //4.3 添加到返回列表
            times.add(time);
            Log.i(TAG, time.toString());
        }
        //5 关闭数据库连接
        dbHelper.close();
        return times;
    }
    ////////////////////////////////////////////////////////////////////////////////
    //
    //  修改
    //
    ////////////////////////////////////////////////////////////////////////////////

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

    /**
     * 更新一个时间段
     * @param old
     * @param fresh
     */
    public void update(TimePeriod old, TimePeriod fresh) {
        // 设置要修改的列和对应的值
        ContentValues values = new ContentValues();
        values.put(TimePeriod.COLUMN_START_HOUR, fresh.getStartHour());
        values.put(TimePeriod.COLUMN_START_MINUTE, fresh.getStartMinute());
        values.put(TimePeriod.COLUMN_END_HOUR, fresh.getEndHour());
        values.put(TimePeriod.COLUMN_END_MINUTE, fresh.getEndMinute());
        values.put(TimePeriod.COLUMN_IS_EVERY_DAY, fresh.getIsEveryDay());
        values.put(TimePeriod.COLUMN_DESCRIPT, fresh.getDescript());
        // 筛选条件
        String where = TimePeriod.COLUMN_START_HOUR + " = ? and "
                + TimePeriod.COLUMN_START_MINUTE + " = ? and "
                + TimePeriod.COLUMN_END_HOUR + " = ? and "
                + TimePeriod.COLUMN_END_MINUTE + " = ? and "
                + TimePeriod.COLUMN_IS_EVERY_DAY + " = ?"
                ;
        // 筛选条件占位符对应的值
        String[] args = {String.valueOf(old.getStartHour()), String.valueOf(old.getStartMinute()),
                String.valueOf(old.getEndHour()), String.valueOf(old.getEndMinute()), String.valueOf(old.getIsEveryDay())};
        update(values, where, args);
    }
    /**
     * 修改数据的基础，所有修改操作都可以将值传入这里进行响应的修改
     * @param values 要修改的值
     * @param where 要修改的行的标识
     * @param args 和where对应的?占位符的值
     */
    private void update(ContentValues values, String where, String[] args) {
        //1. 获取数据库连接
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        //2. 打开可写数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //3. 修改数据库表
        db.update(TimePeriod.TABLENAME, values, where, args);
        //4. 关闭数据库
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    //  删除
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * 根据时间段的id删除在数据库表中删除一个时间段
     * @param id
     */
    public void deleteById(int id) {
        //1. 获取数据库连接
        DBHelper dbHelper = new DBHelper(context, DBHelper.DBNAME);
        //2. 打开可写数据库连接
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //3. 删除条件
        String where = "id = ?";
        //3.1 删除条件占位符对应的值
        String[] args = {String.valueOf(id)};
        //4. 删除操作
        db.delete(TimePeriod.TABLENAME, where, args);
        //5. 关闭数据库连接
        dbHelper.close();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    //  插入
    //
    ////////////////////////////////////////////////////////////////////////////////


    /**
     * 将一个时间段对象保存到数据库表中
     * @param timePeriod
     */
    public void insert(TimePeriod timePeriod) {
        //1. 获取数据库连接
        DBHelper dbHelper = new DBHelper(this.context, DBHelper.DBNAME);
        //2. 打开可写数据库连接
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //3. 插入数据库表的值
        ContentValues values = new ContentValues();
        values.put(TimePeriod.COLUMN_START_HOUR, timePeriod.getStartHour());
        values.put(TimePeriod.COLUMN_START_MINUTE, timePeriod.getStartMinute());
        values.put(TimePeriod.COLUMN_END_HOUR, timePeriod.getEndHour());
        values.put(TimePeriod.COLUMN_END_MINUTE, timePeriod.getEndMinute());
        values.put(TimePeriod.COLUMN_IS_ON, timePeriod.getIsOn());
        values.put(TimePeriod.COLUMN_IS_EVERY_DAY, timePeriod.getIsEveryDay());
        values.put(TimePeriod.COLUMN_DESCRIPT, timePeriod.getDescript());
        values.put(TimePeriod.COLUMN_USERNAME, timePeriod.getUsername());
        //4. 插入操作
        db.insert(TimePeriod.TABLENAME, null, values);
        //5. 关闭数据库连接
        dbHelper.close();
    }

}
