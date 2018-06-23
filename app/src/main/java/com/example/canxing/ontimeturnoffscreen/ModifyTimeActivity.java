package com.example.canxing.ontimeturnoffscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;
import com.example.canxing.ontimeturnoffscreen.util.DevicePolicyUtil;
import com.example.canxing.ontimeturnoffscreen.util.TimeComparing;

import java.io.FileNotFoundException;
import java.util.Calendar;

/**
 * 用于修改已经定义好的活动
 */
public class ModifyTimeActivity extends AppCompatActivity {
    public static final String TAG = "ModifyTimeActivity";
    public static final int RESULTCODE = 0x002;
    private static final int REQUEST_IMAGE_CODE = 0x001;

    private TimePicker startPicker;
    private TimePicker endPicker;
    private EditText descriptText;

    private TimePeriodDB timePeriodDB;
    private TimePeriod oldTimePeriod;

    private int id;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    private String uriString;
    private int cursroPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_time);
        init();
    }

    private void init() {
        timePeriodDB = new TimePeriodDB(this);
        startPicker = findViewById(R.id.modify_start_time_picker);
        endPicker = findViewById(R.id.modify_end_time_picker);
        descriptText = findViewById(R.id.modify_time_descipt);
        startPicker.setIs24HourView(true);
        endPicker.setIs24HourView(true);

        Intent intent = getIntent();
        int id = intent.getIntExtra(TimePeriod.COLUMN_ID, -1);
        oldTimePeriod = timePeriodDB.getTimeById(id);

        startHour = oldTimePeriod.getStartHour();
        startMinute = oldTimePeriod.getStartMinute();
        endHour = oldTimePeriod.getEndHour();
        endMinute = oldTimePeriod.getEndMinute();

        startPicker.setHour(startHour);
        startPicker.setMinute(startMinute);
        endPicker.setHour(endHour);
        endPicker.setMinute(endMinute);

        descriptText.setText(toSpannableString(oldTimePeriod.getDescript()));
    }

    private SpannableString toSpannableString(String text) {
        int start = text.indexOf('#');
        if(start == -1) {
            return new SpannableString(text);
        }
        int end = text.lastIndexOf('#');
        uriString = text.substring(start + 1, end);
        Uri uri = Uri.parse(uriString);
        SpannableString spannableString = null;
        try {
            getContentResolver().openInputStream(uri);
            ImageSpan imageSpan = new ImageSpan(this, uri);
            String[] strs = text.split("#");
            cursroPosition = start;
            if(start == 0) {
                spannableString = new SpannableString("#" + strs[1]);
            } else if(end == text.length() - 1) {
                spannableString = new SpannableString(strs[0] + "#");
            } else {
                spannableString = new SpannableString(strs[0] + "#" + strs[2]);
            }
            spannableString.setSpan(imageSpan, start, start + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE );
        } catch (FileNotFoundException e) {
            String[] strs = text.split("#");
            if(start == 0) {
                spannableString = new SpannableString(strs[1]);
            } else if(end == text.length() - 1) {
                spannableString = new SpannableString(strs[0]);
            } else {
                spannableString = new SpannableString(strs[0] + strs[2]);
            }
        }
        return spannableString;
    }

    public void modifyBtnListener() {
        /*
        获取修改的值
         */
        int modifyStartHour = startPicker.getHour();
        int modifyStartMinute = startPicker.getMinute();
        int modifyEndHour = endPicker.getHour();
        int modifyEndMinute = endPicker.getMinute();
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
                //判断是否进行了修改
//                if (startHour == modifyStartHour && startMinute == modifyStartMinute
//                        && endHour == modifyEndHour && endMinute == modifyEndMinute){
//                    //如果没有修改就不用管它，让它自生自灭吧
//                    setResult(RESULTCODE, null);
//                    finish();
//                } else {
                    //将修改保存在数据库中，并返回
                    TimePeriod timePeriod = new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute);
                    Calendar now = Calendar.getInstance();
                    String nowString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE);
                    boolean isInPeriod =
                            TimeComparing.inPeriod(timePeriod.getStartTime(), timePeriod.getEndTime(), nowString);
                    TimePeriod modifyTimePeriod = new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute);
                    modifyTimePeriod.setDescript(getEditText());
                    timePeriodDB.update(new TimePeriod(startHour, startMinute, endHour, endMinute), modifyTimePeriod);
                            //new TimePeriod(modifyStartHour, modifyStartMinute, modifyEndHour, modifyEndMinute));
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
//                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
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
    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_CODE);
    }
    private void insertImage(Uri uri) {
        Editable text = descriptText.getText();
        uriString = uri.toString();
        ImageSpan imageSpan = new ImageSpan(this, uri);
        SpannableString ss = new SpannableString("#");
        ss.setSpan(imageSpan,0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        text.insert(cursroPosition, ss);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CODE) {
            if(data != null) {
                Uri uri = data.getData();
                insertImage(uri);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meau_save :
                Log.i(TAG, "保存");
                modifyBtnListener();
                return true;
            case R.id.menu_image_choose:
                if(allowChooseImage()) {
                    cursroPosition = descriptText.getSelectionStart();
                    callGallery();
                } else {
                    Toast.makeText(this, "sorry,只能插入一张图片", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meau_save, menu);
        return true;
    }
}
