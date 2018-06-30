package com.example.canxing.ontimeturnoffscreen;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.DBHelper;
import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.Tuple;
import com.example.canxing.ontimeturnoffscreen.util.TwoTuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
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

    private String username;

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

    // 初始化函数，用于初始化控件以及响应等操作的初始化
    private void init() {
        username = getUsername();
        timePeriodDB = new TimePeriodDB(this);
        timeShowView = findViewById(R.id.time_show_view);
        adapter = new TimePeriodAdapter(this, timePeriodDB.getTimesByUsername(username));
        timeShowView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("listview onItemClickListener", "start....");
                TimePeriod timePeriod = (TimePeriod) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, TimePeriodShowActivity.class);
                intent.putExtra(TimePeriod.COLUMN_ID, timePeriod.getId());
                startActivityForResult(intent, REQUEST_TIME_SHOW);
                Log.i("listview onItemClickListener", "over....");
            }
        });
        timeShowView.setAdapter(adapter);
        if(!DevicePolicyUtil.isAdmin(this )) {
            registerDevicePolicy();
        }
    }

    // 获取Sharedperences中的用户名，如果没有返回admin
    private String getUsername(){
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        return sp.getString("username", "admin");
    }
    private String getPassword() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        return sp.getString("password", "admin");
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

    // 开启前台服务，保证程序不中断
    private void startForegroundService() {
        Intent intent = new Intent(this, RegisterReceiverService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

     // 这里接收了保存的时间段，其实可以就在定义界面搞定
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult" , requestCode + "-" + resultCode);
        if(requestCode == SETTINGCODE && resultCode == 1){
            adapter.clear();
            adapter.addAll(timePeriodDB.getTimesByUsername(getUsername()));
            adapter.notifyDataSetInvalidated();
        } else if (requestCode == REQUEST_TIME_SHOW ) {
            adapter.clear();
            adapter.addAll(timePeriodDB.getTimesByUsername(getUsername()));
            adapter.notifyDataSetInvalidated();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 用于在主界面显示的适配器
     */
    class TimePeriodAdapter extends BaseAdapter {
        private List<TimePeriod> times = new ArrayList<>();
        private Context context;

        public TimePeriodAdapter(Context context, List<TimePeriod> times ){
            this.context = context;
            this.times.addAll(times);
        }

        // 清空数据
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
        public List<TimePeriod> getTimes() { return times; }


        /**
         * 本来应该使用 ViewHolder 静态类结合 convertView 缓存来返回试图，但是在处理元素删除时会越界，
         * 关于数组越界问题还没有想好，暂时使用这种原始的方式来显示列表，比较好的是该程序一般不会使用很多元素
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

    private class NetTask extends AsyncTask<String, String, TwoTuple<String, String>> {

        @Override
        protected TwoTuple<String, String> doInBackground(String... strings) {
            HttpURLConnection urlconn = null;
            String text = "";
            try {
                //URL url = new URL("http://192.168.43.142:8080");
                URL url = new URL("http://192.168.50.174:8080");
                urlconn = (HttpURLConnection) url.openConnection();
                urlconn.setRequestMethod("POST");
                urlconn.setDoOutput(true);
                urlconn.setDoInput(true);
                urlconn.setUseCaches(false);
                String message = strings[0];
                urlconn.setChunkedStreamingMode(message.length());
                urlconn.connect();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(urlconn.getOutputStream()));
                Log.i("message", message);
                out.write(message);
                out.flush();
                out.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
                String line = null;
                while((line = in.readLine()) != null) {
                    text += line;
                }

                in.close();
                Log.i("test", text);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(urlconn != null) {
                    urlconn.disconnect();
                }
            }
            Log.i("on execute", "over");
            try {
                JSONObject jsonObject = new JSONObject(strings[0]);
                String task = jsonObject.getString("task");
                return Tuple.towTuple(task, text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Tuple.towTuple(strings[0], text);
        }

        @Override
        protected void onPostExecute(TwoTuple<String, String> stringStringTwoTuple) {
            if(stringStringTwoTuple.first.equals("upload")) {
                uploadHandler(stringStringTwoTuple.second);
            } else if(stringStringTwoTuple.first.equals("download")) {
                downloadHandler(stringStringTwoTuple.second);
            }
        }
    }

    //没有登陆返回false, 登陆了返回true
    private boolean isLogin() {
        return !getUsername().equals("admin");
    }
    private void uploadHandler(String data) {
        Log.i("upalod handler", data);
        if(data.equals("true")) {
            Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadHandler(String data) {
        Log.i("handle download", data);
        try {
            JSONArray jsonArray = new JSONArray(data);
            String str = null;
            for(int i = 0; i < jsonArray.length(); i++) {
                str = jsonArray.getString(i);
                Log.i("str ", str);
                TimePeriod timePeriod = JSONtoTimePeriod(str);
                timePeriodDB.insert(timePeriod);
            }
            adapter.clear();
            adapter.addAll(timePeriodDB.getTimesByUsername(getUsername()));
            adapter.notifyDataSetInvalidated();
            Toast.makeText(this, "下载成功", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(this, "转换错误", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //将一个TimePeriod列表转换为一个JSON数组
    private String timePeriodsToJSON(List<TimePeriod> timePeriods) throws JSONException {
        JSONArray array = new JSONArray();
        for(TimePeriod timePeriod : timePeriods) {
            array.put(timePeriodToJSON(timePeriod));
        }
        return array.toString();
    }

    //将一个TimePeriod转换为JSON字符串
    private String timePeriodToJSON(TimePeriod timePeriod) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TimePeriod.COLUMN_START_HOUR, timePeriod.getStartHour());
        jsonObject.put(TimePeriod.COLUMN_START_MINUTE, timePeriod.getStartMinute());
        jsonObject.put(TimePeriod.COLUMN_END_HOUR, timePeriod.getEndHour());
        jsonObject.put(TimePeriod.COLUMN_END_MINUTE, timePeriod.getEndMinute());
        jsonObject.put(TimePeriod.COLUMN_IS_ON, timePeriod.getIsOn());
        jsonObject.put(TimePeriod.COLUMN_DESCRIPT, timePeriod.getDescript());
        return jsonObject.toString();
    }

    //将一个JSON字符串转换为TimePeriod对象
    private TimePeriod JSONtoTimePeriod(String jsonString) throws JSONException {
        TimePeriod timePeriod = new TimePeriod();
        Log.i("json to timeperiod", jsonString);
        JSONObject jsonObject = new JSONObject(jsonString);
        timePeriod.setStartHour(jsonObject.getInt(TimePeriod.COLUMN_START_HOUR));
        timePeriod.setStartMinute(jsonObject.getInt(TimePeriod.COLUMN_START_MINUTE));
        timePeriod.setEndHour(jsonObject.getInt(TimePeriod.COLUMN_END_HOUR));
        timePeriod.setEndMinute(jsonObject.getInt(TimePeriod.COLUMN_END_MINUTE));
        timePeriod.setIsOn(jsonObject.getInt(TimePeriod.COLUMN_IS_ON));
        timePeriod.setDescript(jsonObject.getString(TimePeriod.COLUMN_DESCRIPT));
        timePeriod.setUsername(getUsername());
        Log.i("json to timeperiod", "over");
        return timePeriod;
    }

    //跳转到登陆界面
    private void callLogin() {
        Intent intent1 = new Intent(this, LoginActivity.class);
        startActivity(intent1);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.time_preiod :
                Intent intent = new Intent(this, TimeSettingActivity.class);
                startActivityForResult(intent, SETTINGCODE);
                break;
            case R.id.menu_download:
                if(isLogin()) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("task", "download");
                        jsonObject.put("username", getUsername());
                        jsonObject.put("password", getPassword());
                        NetTask netTask = new NetTask();
                        netTask.execute(jsonObject.toString());
                    } catch (JSONException e) {
                        Toast.makeText(this, "转换错误", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return true;
                    }
                } else {
                    callLogin();
                }
                break;
            case R.id.menu_upload:
                if(isLogin()) {
                    List<TimePeriod> timePeriods = adapter.getTimes();
                    String timePeriodsJson = "";
                    try {
                        timePeriodsJson = timePeriodsToJSON(timePeriods);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("task", "upload");
                        jsonObject.put("username", getUsername());
                        jsonObject.put("password", getPassword());
                        jsonObject.put("data", timePeriodsJson);
                        NetTask netTask = new NetTask();
                        netTask.execute(jsonObject.toString());
                    } catch (JSONException e) {
                        Toast.makeText(this, "转换错误", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else {
                    callLogin();
                }
                break;
            case R.id.menu_login:
                callLogin();
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
