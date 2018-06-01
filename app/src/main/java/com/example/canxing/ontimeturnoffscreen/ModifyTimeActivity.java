package com.example.canxing.ontimeturnoffscreen;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.sql.Time;
import java.util.Calendar;

/**
 * 用于修改已经定义好的活动
 */
public class ModifyTimeActivity extends AppCompatActivity {
    public static final String TAG = "ModifyTimeActivity";
    public static final int RESULTCODE = 0x002;

    private TimePicker startPicker;
    private TimePicker endPicker;
//    private CheckBox everyday;

    private TimePeriodDB timePeriodDB;

    private int id;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
//    private int isEveryday;
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
        timePeriodDB = new TimePeriodDB(this);
        startPicker = findViewById(R.id.modify_start_time_picker);
        endPicker = findViewById(R.id.modify_end_time_picker);
//        everyday = findViewById(R.id.modify_every_day_radio);
        startPicker.setIs24HourView(true);
        endPicker.setIs24HourView(true);

        /**
         * 因为是修改活动，因此需要从原有的对象数据进行修改
         */
        Intent intent = getIntent();
         startHour = intent.getIntExtra(TimePeriod.COLUMN_START_HOUR, 0);
         startMinute = intent.getIntExtra(TimePeriod.COLUMN_START_MINUTE, 0);
         endHour = intent.getIntExtra(TimePeriod.COLUMN_END_HOUR, 0);
         endMinute = intent.getIntExtra(TimePeriod.COLUMN_END_MINUTE, 0);
//        isEveryday = intent.getIntExtra(TimePeriod.COLUMN_IS_EVERY_DAY, 0);
         id = intent.getIntExtra(TimePeriod.COLUMN_ID, 0);

        /**
         * 将传输过来的值在UI上显示出来
         */
        startPicker.setHour(startHour);
         startPicker.setMinute(startMinute);
         endPicker.setHour(endHour);
         endPicker.setMinute(endMinute);
 //        if(isEveryday == TimePeriod.ON) {
 //            everyday.setChecked(true);
 //        } else {
 //            everyday.setChecked(false);
 //        }
    }

    /**
     * 修改按钮的监听方法
     */
    public void modifyBtnListener() {
        /*
        获取修改的值
         */
        int modifyStartHour = startPicker.getHour();
        int modifyStartMinute = startPicker.getMinute();
        int modifyEndHour = endPicker.getHour();
        int modifyEndMinute = endPicker.getMinute();
//        boolean checkEveryday = everyday.isChecked();
        if(modifyStartHour == modifyEndHour && modifyEndHour == modifyEndMinute) {
            Toast.makeText(this,"起始时间和结束时间不能相等", Toast.LENGTH_LONG).show();
            return;
        }

        String message = "确定在时间" + modifyStartHour + "点" + modifyStartMinute + "分到"
                + modifyEndHour + "点" + modifyEndMinute + "分之间关闭屏幕吗?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                int modifyIsEveryday = -1;
//                if (checkEveryday) {
//                    modifyIsEveryday = TimePeriod.ON;
//                } else {
//                    modifyIsEveryday = TimePeriod.OFF;
//                }
                //判断是否进行了修改
                if (startHour == modifyStartHour && startMinute == modifyStartMinute
                        && endHour == modifyEndHour && endMinute == modifyEndMinute){
//                        && isEveryday == modifyIsEveryday) {
                    //如果没有修改就不用管它，让它自生自灭吧
                    setResult(RESULTCODE, null);
                    finish();
                } else {
                    //将修改保存在数据库中，并返回
                    TimePeriod timePeriod = new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute);
                    Calendar now = Calendar.getInstance();
                    String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
                    boolean isInPeriod =
                            TimeComparing.inPeriod(timePeriod.getStartTime(), timePeriod.getEndTime(), nowString);
                    timePeriodDB.update(new TimePeriod(startHour, startMinute, endHour, endMinute),
                            new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute));
                    if(isInPeriod) {
                        //如果当前时间正好处在修改后的时间段之间
                        new AlertDialog.Builder(ModifyTimeActivity.this)
                                .setMessage("是否立即关闭关闭屏幕?")
                                .setPositiveButton("是", (d, w) -> {
                                    DevicePolicyUtil.lockNow(ModifyTimeActivity.this);
                                })
                                .setNegativeButton("否", (d1, w1)->{
                                    setResult(RESULTCODE, null);
                                    finish();
                                }).create().show();
                    } else {
                        setResult(RESULTCODE, null);
                        finish();

                    }
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 删除按钮的监听事件
     * @param view
     */
    public void deleteTimePeriod(View view) {
        timePeriodDB.deleteById(id);
        setResult(RESULTCODE, null);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meau_save :
                Log.i(TAG, "保存");
                modifyBtnListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meau, menu);
        return true;
    }
}
