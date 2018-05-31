package com.example.canxing.ontimeturnoffscreen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.util.Calendar;

public class TimeSettingActivity extends AppCompatActivity {
    public static final String TAG = "TimeSettingActivity";
    public static final int RESULTCODE = 0x001;
    private TimePicker startTime;
    private TimePicker endTime;
    private CheckBox everyday;

    private TimePeriodDB timePeriodDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_setting);
        init();

    }

    /**
     * 保存响应事件
     */
    public void saveBtnListener() {
        int startHour = startTime.getHour();
        int startMinute = startTime.getMinute();

        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();
        boolean isEquals = TimeComparing.isEquals(startHour + ":" + startMinute, endHour + ":" + endMinute);
        if(isEquals) {
            Toast.makeText(this,"起始时间和结束时间不能相等", Toast.LENGTH_LONG).show();
            return;
        }
        boolean everyDay = everyday.isChecked();
        String message = "却定在时间" + startHour + "点" + startMinute + "分到" + endHour + "点" + endMinute + "分之间关闭屏幕吗?";

        //作为是否立即关闭屏幕的判断条件
        boolean isCloseScreenNow = false;
        //验证新定义的时间段是否和已有时间段重合
        //弹出对话框让用户确认时间段
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //验证当前时间是否在定义时间段之内，如果是，则立即关闭屏幕并保存到数据库中
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
                timePeriodDB.insert(timePeriod);
                if(isInPeriod) {
                    // 如果点击保存的时间是在要关闭的时间段内，又弹出一个对话框，让用户选择是否立即关闭屏幕
                    new AlertDialog.Builder(TimeSettingActivity.this).setMessage("是否立即关闭屏幕?")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DevicePolicyUtil.lockNow(TimeSettingActivity.this);
                                    setResult(RESULTCODE, null);
                                    finish();
                                }
                            })
                            .setNegativeButton("否", (DialogInterface d, int w)->{
                                setResult(RESULTCODE, null);
                                finish();
                            }).create().show();
                } else {
                    setResult(RESULTCODE, null);
                    finish();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();



//        Intent intent = new Intent();
//        intent.putExtra(TimePeriod.COLUMN_START_HOUR, startHour);
//        intent.putExtra(TimePeriod.COLUMN_START_MINUTE, startMinute);
//        intent.putExtra(TimePeriod.COLUMN_END_HOUR, endHour);
//        intent.putExtra(TimePeriod.COLUMN_END_MINUTE, endMinute);
//        intent.putExtra(TimePeriod.COLUMN_IS_ON, timePeriod.getIsOn());
//        intent.putExtra(TimePeriod.COLUMN_IS_EVERY_DAY, timePeriod.getIsEveryDay());
//        setResult(1, intent);
    }

    private void init() {
        startTime = findViewById(R.id.start_time_picker);
        endTime = findViewById(R.id.end_time_picker);
        everyday = findViewById(R.id.every_day_radio);
        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        timePeriodDB = new TimePeriodDB(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meau_save :
                Log.i(TAG, "保存");
                saveBtnListener();
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
