package com.mango.puppet;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mango.puppet.log.LogManager;
import com.mango.puppet.log.LogUtil;
import com.mango.puppet.log.i.ILog;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

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

        assertEquals("com.mango.puppet.test", appContext.getPackageName());
    }
    @Test
    public void ILogUsingTest() {
        String log = "124test";
        LogManager mLog = LogManager.getInstance();
        mLog.setNewLogListener(new ILog.ILogListener(){
            @Override
            public void onNewLog(String log) {
                Log.e("writeLog",log);
            }
        });
        mLog.recordLog(log);
//        File file = new File(LogUtil.getLogStorePath());
    }
}
