package com.example.canxing.ontimeturnoffscreen;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.canxing.ontimeturnoffscreen.db.TimePeriodDB;
import com.example.canxing.ontimeturnoffscreen.model.TimePeriod;

import java.io.FileNotFoundException;

public class TimePeriodShowActivity extends AppCompatActivity {
    public static final int RESULT_CDOE_TIME_DELETE = 0x001;

    private static final int REQUEST_MODIFY_TIME = 0x001;

    private static final String TAG = "Time Period Show Activity";

    private TimePeriod timePeriod;
    private TimePeriodDB timePeriodDB;
    private TextView timeText;
    private TextView descriptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_period_show);
        init();
    }
    private void init() {
        Intent intent = getIntent();
        int id = intent.getIntExtra(TimePeriod.COLUMN_ID, -1);
        timePeriodDB = new TimePeriodDB(this);
        timePeriod = timePeriodDB.getTimeById(id);

        timeText = findViewById(R.id.show_time_text);
        descriptText = findViewById(R.id.show_time_descript);

        timeText.setText(timePeriod.getStartTime() + "-" + timePeriod.getEndTime());
        descriptText.setText(toSpannable(timePeriod.getDescript()));
    }

    //将时间段描述转换为SpannableString，如果其中有图片标识就转换为ImageSpan插入其中
    private SpannableString toSpannable(String text) {
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf('#');
        if(start == -1) {
            return spannableString;
        }
        int end = text.lastIndexOf('#');
        String uriString = text.substring(start + 1, end);
        Uri uri = Uri.parse(uriString);
        try {
            getContentResolver().openInputStream(uri);
            ImageSpan imageSpan = new ImageSpan(this, uri);
            spannableString.setSpan(imageSpan, start, end + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }
        return spannableString;
    }
    private void callModifyTime() {
        Intent intent = new Intent(this, ModifyTimeActivity.class);
        intent.putExtra(TimePeriod.COLUMN_ID, timePeriod.getId());
        startActivityForResult(intent, REQUEST_MODIFY_TIME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_MODIFY_TIME && resultCode == ModifyTimeActivity.RESULTCODE) {
            Log.i(TAG, "on activity result");
            setResult(RESULT_CDOE_TIME_DELETE);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_time_period_delete:
                timePeriodDB.deleteById(timePeriod.getId());
                setResult(RESULT_CDOE_TIME_DELETE);
                finish();
                break;
            case R.id.menu_time_period_modify:
                callModifyTime();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_modify, menu);
        return true;
    }
}
