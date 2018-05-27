package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    static final String LOG_TAG = "ScreenOffActivity";

    private Button screenOffBtn;
    private Button addBtn;
    private DevicePolicyManager mDPM;
    private ScreenOnReceiver receiver;

    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter screenFilte = new IntentFilter(Intent.ACTION_SCREEN_ON);
        receiver = new ScreenOnReceiver();
        registerReceiver(receiver, screenFilte);

        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        screenOffBtn = findViewById(R.id.screen_off_btn);
        addBtn = findViewById(R.id.add_time);
        addBtn.setOnClickListener((view)->{
            Intent intent = new Intent(this, TimeSettingActivity.class);
            startActivityForResult(intent, 1);
        });

        screenOffBtn.setOnClickListener((view)->{
            ComponentName mDeviceAdminSample = new ComponentName(this, ScreenOffAdminReceiver.class);
            boolean isAdmin = mDPM.isAdminActive(mDeviceAdminSample);
            Log.i("isAdmin", isAdmin + "");
            if(isAdmin) {
                mDPM.lockNow();
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "我是不是很帅");
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult" , requestCode + "-" + resultCode);
        if(requestCode == 1 && resultCode == 1){
            if(data != null){
                Log.i("onActivityResult", "data not null");
                startHour = data.getIntExtra("startHour", 0);
                startMinute = data.getIntExtra("startMinute", 0);
                endHour = data.getIntExtra("endHour", 0);
                endMinute = data.getIntExtra("endMinute", 0);
                Log.i("onActivityResult", "");
                Log.i("start time", startHour + " : " + startMinute);
                Log.i("end time", endHour + " : " + endMinute);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
