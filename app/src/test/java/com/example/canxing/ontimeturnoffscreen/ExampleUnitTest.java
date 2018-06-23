package com.example.canxing.ontimeturnoffscreen;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "username");
        jsonObject.put("password", "password");
        System.out.println(jsonObject.toString());
    }
}