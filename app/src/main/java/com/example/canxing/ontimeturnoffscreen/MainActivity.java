package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "ScreenOffActivity";

    private ListView timeShowView;
    private Button addBtn;
    private TimePeriodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clearDB();
        init();
        startForegroundService();
    }

    /**
     * 清空数据库中的时间段
     */
    private void clearDB() {
        DBHelper dbHelper = new DBHelper(this, DBHelper.DBNAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TimePeriod.TABLENAME, null, null);
        dbHelper.close();
    }

    /**
     * 返回数据库中的所有时间段
     * @return
     */
    private List<TimePeriod> getTimes() {
        List<TimePeriod> times = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(this, DBHelper.DBNAME);
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
            Log.i(TAG + "getTimes", time.toString());
        }
        dbHelper.close();
        return times;
    }

    /**
     * 初始化函数，用于初始化控件以及响应等操作的初始化
     */
    private void init() {
        addBtn = findViewById(R.id.add_time);
        timeShowView = findViewById(R.id.time_show_view);
        adapter = new TimePeriodAdapter(this, getTimes());
        timeShowView.setAdapter(adapter);
        addBtn.setOnClickListener((view)->{
            Intent intent = new Intent(this, TimeSettingActivity.class);
            startActivityForResult(intent, 1);
        });
        if(!DevicePolicyUtil.isAdmin(this )) {
            registerDevicePolicy();
        }
    }

    /**
     * 前往系统注册设备管理器界面
     * 这个应该只出现一次，注册一次之后就不用重复注册
     */
    private void registerDevicePolicy(){
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DevicePolicyUtil.getComponentName(this));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "我是不是很帅");
        startActivityForResult(intent, 0);
    }

    /**
     * 开启前台服务，保证程序不中断
     */
    private void startForegroundService() {
        Intent intent = new Intent(this, RegisterReceiverService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 这里接收了保存的时间段，其实可以就在定义界面搞定
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult" , requestCode + "-" + resultCode);
        if(requestCode == 1 && resultCode == 1){
            if(data != null){
                TimePeriod time = new TimePeriod();
                time.setStartHour(data.getIntExtra(TimePeriod.COLUMN_START_HOUR, 0));
                time.setStartMinute(data.getIntExtra(TimePeriod.COLUMN_START_MINUTE, 0));
                time.setEndHour(data.getIntExtra(TimePeriod.COLUMN_END_HOUR, 0));
                time.setEndMinute(data.getIntExtra(TimePeriod.COLUMN_END_MINUTE, 0));
                time.setIsOn(data.getIntExtra(TimePeriod.COLUMN_IS_ON, 0));
                time.setIsEveryDay(data.getIntExtra(TimePeriod.COLUMN_IS_EVERY_DAY, 0));
                adapter.add(time);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    class TimePeriodAdapter extends BaseAdapter {
        private List<TimePeriod> times = new ArrayList<>();
        private Context context;

        public TimePeriodAdapter(Context context, List<TimePeriod> times ){
            this.context = context;
            this.times.addAll(times);
        }
        public void add(TimePeriod time) {
            times.add(time);
        }
        @Override
        public int getCount() {
            return times.size();
        }

        @Override
        public TimePeriod getItem(int position) {
            return times.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.time_show,null);
                holder = new ViewHolder();
                holder.timeText = convertView.findViewById(R.id.time_show_text);
                holder.onSwitch = convertView.findViewById(R.id.on_switch);
                holder.eveyDayText = convertView.findViewById(R.id.every_day_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TimePeriod time = times.get(position);
            String period = time.getStartTime() + "-" + time.getEndTime();
            holder.timeText.setText(period);
            if(time.getIsEveryDay() == TimePeriod.ON){
                holder.eveyDayText.setText("每天");
            } else {
                holder.eveyDayText.setText("一次");
            }
            if(time.getIsOn() == TimePeriod.ON) {
                holder.onSwitch.setChecked(true);
            } else {
                holder.onSwitch.setChecked(false);
            }
            return convertView;
        }

        public void addAll(List<TimePeriod> times) {
            this.times.addAll(times);
        }

        class ViewHolder {
            TextView timeText;
            Switch onSwitch;
            TextView eveyDayText;
        }
    }
}
