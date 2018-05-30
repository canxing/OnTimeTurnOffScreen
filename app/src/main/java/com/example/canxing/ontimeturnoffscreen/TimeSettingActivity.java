package com.example.canxing.ontimeturnoffscreen;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.util.Calendar;

public class TimeSettingActivity extends AppCompatActivity {
    public static final int RESULTCODE = 0x001;
    private TimePicker startTime;
    private TimePicker endTime;
    private CheckBox everydayRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_setting);
        init();

    }

    /**
     * 保存按钮的响应事件
     * @param view
     */
    public void saveBtnListener(View view) {
        int startHour = startTime.getHour();
        int startMinute = startTime.getMinute();

        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();
        boolean isEquals = TimeComparing.isEquals(startHour + ":" + startMinute, endHour + ":" + endMinute);
        if(isEquals) {
            Toast.makeText(this,"起始时间和结束时间不能相等", Toast.LENGTH_LONG).show();
            return;
        }
        boolean everyDay = everydayRadio.isChecked();
        TimePeriod timePeriod = new TimePeriod(startHour, startMinute, endHour, endMinute);
        timePeriod.setIsOn(TimePeriod.ON);
        if(everyDay) {
            timePeriod.setIsEveryDay(TimePeriod.ON);
        } else {
            timePeriod.setIsEveryDay(TimePeriod.OFF);
        }
        Calendar now = Calendar.getInstance();
        String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
        boolean isInPeriod = TimeComparing.inPeriod(timePeriod.getStartTime(), timePeriod.getEndTime(), nowString);
//                (TimeComparing.isAfter(nowString, timePeriod.getStartTime()) ||
//                TimeComparing.isEquals(nowString, timePeriod.getStartTime()))
//                &&
//                (TimeComparing.isBefore(nowString, timePeriod.getEndTime()) ||
//                TimeComparing.isEquals(nowString, timePeriod.getEndTime()));
         // 如果点击保存的时间是在要关闭的时间段内
        if(isInPeriod) {
            DevicePolicyUtil.lockNow(this);
        }
        DBHelper dbHelper = new DBHelper(this, DBHelper.DBNAME);
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

//        Intent intent = new Intent();
//        intent.putExtra(TimePeriod.COLUMN_START_HOUR, startHour);
//        intent.putExtra(TimePeriod.COLUMN_START_MINUTE, startMinute);
//        intent.putExtra(TimePeriod.COLUMN_END_HOUR, endHour);
//        intent.putExtra(TimePeriod.COLUMN_END_MINUTE, endMinute);
//        intent.putExtra(TimePeriod.COLUMN_IS_ON, timePeriod.getIsOn());
//        intent.putExtra(TimePeriod.COLUMN_IS_EVERY_DAY, timePeriod.getIsEveryDay());
//        setResult(1, intent);
        setResult(RESULTCODE, null);
        finish();
    }

    private void init() {
        startTime = findViewById(R.id.start_time_picker);
        endTime = findViewById(R.id.end_time_picker);
        everydayRadio = findViewById(R.id.every_day_radio);
    }

}
