package com.example.canxing.ontimeturnoffscreen;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class TimeSettingActivity extends AppCompatActivity {
    private TimePicker startTime;
    private TimePicker endTime;

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

        //Log.i("start hour", startHour + " : " + startMinute);
        //Log.i("end hour", endHour + " : " + endMinute);

        Intent intent = new Intent();
        intent.putExtra("startHour", startHour);
        intent.putExtra("startMinute", startMinute);
        intent.putExtra("endHour", endHour);
        intent.putExtra("endMinute", endMinute);
        setResult(1, intent);
        finish();
    }

    private void init() {
        startTime = findViewById(R.id.start_time_picker);
        endTime = findViewById(R.id.end_time_picker);
    }

}
