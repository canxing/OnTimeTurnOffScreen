package com.example.canxing.ontimeturnoffscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.canxing.ontimeturnoffscreen.util.Tuple;
import com.example.canxing.ontimeturnoffscreen.util.TwoTuple;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputPassword;
    private Button login;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }
    private void init() {
        inputUsername = findViewById(R.id.login_username);
        inputPassword = findViewById(R.id.login_password);
        login = findViewById(R.id.login_login);
        register = findViewById(R.id.login_register);
        login.setOnClickListener((view)->{
            loginEvent();
        });
        register.setOnClickListener((view)->{
            registerEvent();
        });
    }

    //登陆事件，接受用户输入并发送服务器进行验证
    private void loginEvent(){
        if(isEmpty(inputUsername) || isEmpty(inputPassword)){
            Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_LONG).show();
            return ;
        }
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        LoginTask loginTask = new LoginTask();
        loginTask.execute("login", username, password);
    }
    //注册事件，接受用户输入发送到服务器进行注册
    private void registerEvent(){
        if(isEmpty(inputUsername) || isEmpty(inputPassword)){
            Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_LONG).show();
            return ;
        }
        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        LoginTask registerTask = new LoginTask();
        registerTask.execute("register", username, password);
    }


    //对登陆操作的返回结果进行处理
    //isSuccess为'true'或'false'
    private void login(String isSuccess) {
        if(isSuccess.equals("true")) {
            loginSuccess();
        } else {
            loginFail();
        }
    }

    /**
     * 对注册结果进行处理
     * @param isRegister 'true'或者'false'
     */
    private void register(String isRegister) {
        if(isRegister.equals("true")) {
            registerSuccess();
        } else {
            registerFail();
        }
    }

    //登陆成功执行的操作
    private void loginSuccess() {
        Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
        writeUser();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //登陆失败执行的操作
    private void loginFail() {
        Log.i("login fail", "");
        Toast.makeText(this, "用户名密码不匹配", Toast.LENGTH_LONG).show();
    }

    //注册成功执行的操作
    private void registerSuccess() {
        Toast.makeText(this, "注册成功，你已登陆", Toast.LENGTH_SHORT).show();
        writeUser();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //注册失败进行的操作
    private void registerFail(){
        Log.i("register fail", "");
        Toast.makeText(this, "该用户名已经有人使用，请重新输入", Toast.LENGTH_LONG).show();
    }

    /**
     * 向 SharedPreferences中写入登陆者的用户名
     * 只能用于登陆或者注册成功的情况
     */
    private void writeUser() {
        Log.i("write user", "starting..");
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("username", inputUsername.getText().toString());
        editor.putString("password", inputPassword.getText().toString());
        editor.commit();
        Log.i("write user", "end...");
    }

    /**
     * 判断EditText是否有输入
     * @param editText
     * @return
     */
    private boolean isEmpty(EditText editText){
        if(editText == null){
            return true;
        } else if(editText.getText().toString().equals("")){
            return true;
        } else {
            return false;
        }
    }
    /**
     * 登陆和注册任务，执行将数据发送到服务器进行注册和登陆
     */
    private class LoginTask extends AsyncTask<String, String, TwoTuple<String, String>> {
        @Override
        protected TwoTuple<String, String> doInBackground(String... strings) {
            HttpURLConnection urlconn = null;
            String text = "";
            TwoTuple<String, String> result = null;
            try {
                URL url = new URL("http://192.168.43.142:8080");
                urlconn = (HttpURLConnection) url.openConnection();
                urlconn.setRequestMethod("POST");
                urlconn.setDoOutput(true);
                urlconn.setDoInput(true);
                urlconn.setUseCaches(false);

                String message = "";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("task", strings[0]);
                jsonObject.put("username", strings[1]);
                jsonObject.put("password", strings[2]);
                message = jsonObject.toString();

                urlconn.setChunkedStreamingMode(message.length());
                urlconn.connect();
                OutputStream outputStream = urlconn.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream));

                Log.i("message", message);
                out.write(message);
                out.flush();
                outputStream.close();
                out.close();

                InputStream inputStream = urlconn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while((line = in.readLine()) != null) {
                    text += line;
                }
                inputStream.close();
                in.close();
                Log.i("on execute", "text");
                result = Tuple.towTuple(strings[0], text);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                result = Tuple.towTuple("error", "网络错误，请检查后重试");
            } catch (IOException e) {
                e.printStackTrace();
                result = Tuple.towTuple("error", "网络错误，请检查后重试");
            } catch (JSONException e) {
                e.printStackTrace();
                result = Tuple.towTuple("error", "数据处理错误");
            } finally {
                if(urlconn != null) {
                    urlconn.disconnect();
                }
            }
            Log.i("on execute", "over");
            return result;
        }

        @Override
        protected void onPostExecute(TwoTuple<String, String> s) {
            if(s.first.equals("login")){
                login(s.second);
            } else if(s.first.equals("register")){
                register(s.second);
            } else {
                Toast.makeText(LoginActivity.this, s.second, Toast.LENGTH_LONG).show();
            }
        }
    }
}
