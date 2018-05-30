package com.example.canxing.ontimeturnoffscreen;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.sql.Time;
import java.util.Calendar;

/**
 * 用于修改已经定义好的活动
 */
public class ModifyTimeActivity extends AppCompatActivity {
    public static final int RESULTCODE = 0x002;

    private TimePicker startPicker;
    private TimePicker endPicker;
    private CheckBox everyday;

    int startHour;
    int startMinute;
    int endHour;
    int endMinute;
    int isEveryday;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_time);
        init();
    }

    /**
     * 初始化活动
     */
    private void init() {
        startPicker = findViewById(R.id.modify_start_time_picker);
        endPicker = findViewById(R.id.modify_end_time_picker);
        everyday = findViewById(R.id.modify_every_day_radio);

        /**
         * 因为是修改活动，因此需要从原有的对象数据进行修改
         */
        Intent intent = getIntent();
         startHour = intent.getIntExtra(TimePeriod.COLUMN_START_HOUR, 0);
         startMinute = intent.getIntExtra(TimePeriod.COLUMN_START_MINUTE, 0);
         endHour = intent.getIntExtra(TimePeriod.COLUMN_END_HOUR, 0);
         endMinute = intent.getIntExtra(TimePeriod.COLUMN_END_MINUTE, 0);
         isEveryday = intent.getIntExtra(TimePeriod.COLUMN_IS_EVERY_DAY, 0);

        /**
         * 将传输过来的值在UI上显示出来
         */
        startPicker.setHour(startHour);
         startPicker.setMinute(startMinute);
         endPicker.setHour(endHour);
         endPicker.setMinute(endMinute);
         if(isEveryday == TimePeriod.ON) {
             everyday.setChecked(true);
         } else {
             everyday.setChecked(false);
         }
    }

    /**
     * 修改按钮的监听方法
     * @param view
     */
    public void modifyBtnListener(View view) {
        /*
        获取修改的值
         */
        int modifyStartHour = startPicker.getHour();
        int modifyStartMinute = startPicker.getMinute();
        int modifyEndHour = endPicker.getHour();
        int modifyEndMinute = endPicker.getMinute();
        boolean checkEveryday = everyday.isChecked();
        int modifyIsEveryday = -1;
        if (checkEveryday) {
            modifyIsEveryday = TimePeriod.ON;
        } else {
            modifyIsEveryday = TimePeriod.OFF;
        }
        //判断是否进行了修改
        if (startHour == modifyStartHour && startMinute == modifyStartMinute
                && endHour == modifyEndHour && endMinute == modifyEndMinute
                && isEveryday == modifyIsEveryday) {
            //如果没有修改就不用管它，让它自生自灭吧
        } else {
            //将修改保存在数据库中，并返回
            TimePeriod timePeriod = new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute);
            Calendar now = Calendar.getInstance();
            String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
            boolean isInPeriod =
                    TimeComparing.inPeriod(timePeriod.getStartTime(), timePeriod.getEndTime(), nowString);
//                    TimeComparing.isAfter(nowString, timePeriod.getStartTime()) ||
//                    TimeComparing.isEquals(nowString, timePeriod.getStartTime()))
//                    &&
//                    (TimeComparing.isBefore(nowString, timePeriod.getEndTime()) ||
//                            TimeComparing.isEquals(nowString, timePeriod.getEndTime()));
            if(isInPeriod) {
                //如果当前时间正好处在修改后的时间段之间
                DevicePolicyUtil.lockNow(this);
            }
            DBHelper dbHelper = new DBHelper(this, DBHelper.DBNAME);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(TimePeriod.COLUMN_START_HOUR, modifyStartHour);
            values.put(TimePeriod.COLUMN_START_MINUTE, modifyStartMinute);
            values.put(TimePeriod.COLUMN_END_HOUR, modifyEndHour);
            values.put(TimePeriod.COLUMN_END_MINUTE, modifyEndMinute);
            values.put(TimePeriod.COLUMN_IS_EVERY_DAY, modifyIsEveryday);
            String where = TimePeriod.COLUMN_START_HOUR + " = ? and "
                    + TimePeriod.COLUMN_START_MINUTE + " = ? and "
                    + TimePeriod.COLUMN_END_HOUR + " = ? and "
                    + TimePeriod.COLUMN_END_MINUTE + " = ? and "
                    + TimePeriod.COLUMN_IS_EVERY_DAY + " = ?"
                    ;
            String[] args = {String.valueOf(startHour), String.valueOf(startMinute),
                    String.valueOf(endHour), String.valueOf(endMinute), String.valueOf(isEveryday)};
            db.update(TimePeriod.TABLENAME, values, where, args);
            dbHelper.close();
        }
        setResult(RESULTCODE, null);
        finish();
    }
}
