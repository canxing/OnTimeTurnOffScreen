package com.example.canxing.ontimeturnoffscreen.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.example.canxing.ontimeturnoffscreen.ScreenOffAdminReceiver;

/**
 * 设备管理器的工具集合
 */
public class DevicePolicyUtil {
    private static DevicePolicyManager mDPM;
    private static ComponentName mDeviceAdminSample;

    public static void lockNow(Context context) {
        init(context);
        mDPM.lockNow();
    }

    public static ComponentName getComponentName(Context context) {
        init(context);
        return mDeviceAdminSample;
    }
    /**
     * 判断这个App是否是系统应用
     * @return
     */
    public static boolean isAdmin(Context context) {
        init(context);
        return mDPM.isAdminActive(mDeviceAdminSample);
    }
    private static void init(Context context) {
        if(mDPM == null || mDeviceAdminSample == null) {
            mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            mDeviceAdminSample =
                    new ComponentName(context, ScreenOffAdminReceiver.class);
        }
    }
}
