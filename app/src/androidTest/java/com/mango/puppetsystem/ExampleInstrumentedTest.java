package com.mango.puppetsystem;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mango.puppet.systemplugin.SystemPluginManager;
import com.mango.puppet.systemplugin.i.ISystemPluginExecute;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();



        assertEquals("com.mango.puppetsystem", appContext.getPackageName());
    }

    @Test
    public void testDemo() {

        final String packageName = "com.tencent.mm";
        SystemPluginManager.getInstance().changeForegroundApplication(packageName, new ISystemPluginExecute.ISystemPluginResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                if (isSucceed) {
                    String foregroundApp = getForegroundApp();
                    Log.d("ExampleInstrumentedTest", "foregroundApp:" + foregroundApp);
                    assertTrue(foregroundApp.contains(packageName));
                }
            }
        });
    }

    public static String getForegroundApp() {
        return execRootCmd("dumpsys window windows | grep mFocusedApp");
    }

    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                result += line;
                result += "\n";
            }
            if (!TextUtils.isEmpty(result)) {
                result = result.substring(0, result.length() - 1);
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
