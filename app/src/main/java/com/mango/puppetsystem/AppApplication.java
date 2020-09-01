package com.mango.puppetsystem;

import android.app.Application;

import com.mango.puppet.tool.PreferenceUtils;
import com.mango.puppet.tool.TextTool;
import com.umeng.commonsdk.UMConfigure;

public class AppApplication extends Application {

    public static AppApplication instance;
    public static String CHANNEL = "HZ";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        UMConfigure.init(this, "5f336968d309322154776f9e", CHANNEL, 0, null);
        PreferenceUtils.getInstance().init(this);
        TextTool.resetAllLog();
//        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }
}
