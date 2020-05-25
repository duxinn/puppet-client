package com.mango.puppetsystem;

import android.app.Application;
import android.util.Log;

import com.mango.puppet.systemplugin.SystemPluginManager;
import com.mango.puppet.systemplugin.i.ISystemPluginListener;

public class AppApplication extends Application {

    public static AppApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
