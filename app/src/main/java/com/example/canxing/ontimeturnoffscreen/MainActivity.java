package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "ScreenOffActivity";
    public static final int SETTINGCODE = 0x001;
    public static final int MODIFYCODE = 0x002;
    private static final int REQUEST_TIME_SHOW = 0x003;

    private ListView timeShowView;
    private TimePeriodAdapter adapter;

    private TimePeriodDB timePeriodDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //clearDB();
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

    /**
     * 初始化函数，用于初始化控件以及响应等操作的初始化
     */
    private void init() {
        timePeriodDB = new TimePeriodDB(this);
        timeShowView = findViewById(R.id.time_show_view);
        adapter = new TimePeriodAdapter(this, timePeriodDB.getTimes());
        timeShowView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("listview onItemClickListener", "start....");
                TimePeriod timePeriod = (TimePeriod) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, TimePeriodShowActivity.class);
                intent.putExtra(TimePeriod.COLUMN_ID, timePeriod.getId());
                startActivityForResult(intent, REQUEST_TIME_SHOW);
//                intent.putExtra(TimePeriod.COLUMN_START_HOUR, timePeriod.getStartHour());
//                intent.putExtra(TimePeriod.COLUMN_START_MINUTE, timePeriod.getStartMinute());
//                intent.putExtra(TimePeriod.COLUMN_END_HOUR, timePeriod.getEndHour());
//                intent.putExtra(TimePeriod.COLUMN_END_MINUTE, timePeriod.getEndMinute());
//                intent.putExtra(TimePeriod.COLUMN_IS_EVERY_DAY, timePeriod.getIsEveryDay());
//                intent.putExtra(TimePeriod.COLUMN_ID, timePeriod.getId());
//                startActivityForResult(intent, MODIFYCODE);
                Log.i("listview onItemClickListener", "over....");
            }
        });
        timeShowView.setAdapter(adapter);
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
        if(requestCode == SETTINGCODE && resultCode == 1){
            adapter.clear();
            adapter.addAll(timePeriodDB.getTimes());
            adapter.notifyDataSetInvalidated();
        } else if (requestCode == REQUEST_TIME_SHOW ) {
            adapter.clear();
            adapter.addAll(timePeriodDB.getTimes());
            adapter.notifyDataSetInvalidated();
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

        /**
         * 清空数据
         */
        public void clear() {
            times.clear();
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


        /**
         * 本来应该使用 ViewHolder 静态类结合 convertView 缓存来返回试图，但是在处理元素删除时会越界，
         * 关于数组越界问题还没有想好，暂时使用这种原始的方式来显示列表，比较好的是该程序一般不会使用很多元素
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item = LayoutInflater.from(context).inflate(R.layout.time_show, null);
            TextView timetext = item.findViewById(R.id.time_show_text);
            Switch onSwitch = item.findViewById(R.id.on_switch);
            TextView descipt = item.findViewById(R.id.descript);
            TimePeriod time = times.get(position);
            timetext.setText(time.getStartTime() + "-" + time.getEndTime());
            descipt.setText(time.getDescript());
            if(time.getIsOn() == TimePeriod.ON) {
                onSwitch.setChecked(true);
            } else {
                onSwitch.setChecked(false);
            }
            onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //Log.i(TAG + " switch", isChecked + " " + getItem(position).getId());
                    timePeriodDB.updateTimePeriodOnById(getItem(position).getId(), isChecked);
                }
            });
            return item;


        }

        public void addAll(List<TimePeriod> times) {
            this.times.addAll(times);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.time_preiod :
                Intent intent = new Intent(this, TimeSettingActivity.class);
                startActivityForResult(intent, SETTINGCODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }
}
