package com.example.canxing.ontimeturnoffscreen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;


/**
 * 时间段新增界面
 */
public class TimeSettingActivity extends AppCompatActivity {
    public static final String TAG = "TimeSettingActivity";
    public static final int RESULTCODE = 0x001;
    private static final int REQUEST_IMAGE_CODE = 0x002;
    private TimePicker startTime;
    private TimePicker endTime;
    private EditText descriptText;
    private TimePeriodDB timePeriodDB;

    private int cursroPosition;
    private String uriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_setting);
        init();

    }

    // 保存响应事件
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
        String message = "确定在时间" + startHour + "点" + startMinute + "分到" + endHour + "点" + endMinute + "分之间关闭屏幕吗?";

        //作为是否立即关闭屏幕的判断条件
        boolean isCloseScreenNow = false;
        //验证新定义的时间段是否和已有时间段重合
        //弹出对话框让用户确认时间段
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TimePeriod timePeriod = new TimePeriod(startHour, startMinute, endHour, endMinute);
                timePeriod.setIsOn(TimePeriod.ON);
                timePeriod.setDescript(getEditText());
                timePeriod.setUsername(getUsername());
                Calendar now = Calendar.getInstance();
                String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
                boolean isInPeriod = TimeComparing.inPeriod(timePeriod.getStartTime(), timePeriod.getEndTime(), nowString);
                Log.i(TAG, timePeriod.toString());
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
    }

    /**
     * 获取时间段的输入描述，因为描述中可能有图片，所以需要进行转换
     * @return
     */
    private String getEditText() {
        String text = descriptText.getText().toString();
        String result = "";
        int index = text.indexOf('#');
        if( index == -1) {
            result = text;
        } else {
            uriString = "#" + uriString + "#";
            if (index == 0) {
                result = uriString + text.substring(1);
            } else if(index == text.length() - 1) {
                result = text + uriString.substring(1);
            } else {
                String[] strs = text.split("#");
                result = strs[0] + uriString + strs[1];
            }
        }
        Log.i(TAG, result);
        return result;
    }

    //获取SharedPreference中保存的用户名
    private String getUsername() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        return sp.getString("username", "admin");
    }

    private void init() {
        startTime = findViewById(R.id.start_time_picker);
        endTime = findViewById(R.id.end_time_picker);
        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        descriptText = findViewById(R.id.time_period_description);

        timePeriodDB = new TimePeriodDB(this);
    }

    //使用startActivityForResult跳转到系统图库选择图片返回
    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_CODE);
    }

    //由于技术原因，目前只能插入一张图片，这里用来判断是否已经插入了图片
    private boolean allowChooseImage() {
        if(uriString == null) {
            return true;
        }
        Editable text = descriptText.getText();
        if(cursroPosition >= text.length()) {
            return true;
        }
        char ch = text.charAt(cursroPosition);
        if( ch == '#') {
            return false;
        } else {
            return true;
        }
    }
    //把一个Uri代表的图片插入文字中
    private void insertImage(Uri uri) {
        insertImage(uri.toString());
    }
    private void insertImage(String uri) {
        Editable text = descriptText.getText();
        uriString = uri;
        ImageSpan imageSpan = new ImageSpan(this, BitmapFactory.decodeFile(uri));
        SpannableString ss = new SpannableString("#");
        ss.setSpan(imageSpan,0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        text.insert(cursroPosition, ss);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CODE) {
            if(data != null) {
                Uri uri = data.getData();
                String fileName = uri.toString().substring(uri.toString().lastIndexOf('/'));
                String filePath = getFilesDir().toString() + fileName;
                try(
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath));
                    InputStream in = getContentResolver().openInputStream(uri);){
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    while((len = in.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                        out.flush();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                insertImage(filePath);
                Log.i(TAG + " On Activity Result", data.getData().toString());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meau_save :
                Log.i(TAG, "保存");
                saveBtnListener();
                break;
            case R.id.menu_image_choose:
                if(allowChooseImage()) {
                    cursroPosition = descriptText.getSelectionStart();
                    callGallery();
                } else {
                    Toast.makeText(this, "sorry,只能插入一张图片", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meau_save, menu);
        return true;
    }
}
