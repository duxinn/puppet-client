package com.mango.puppetsystem;

import android.app.Application;
import android.util.Log;

import com.mango.puppet.systemplugin.SystemPluginManager;
import com.mango.puppet.systemplugin.i.ISystemPluginListener;

public class AppApplication extends Application implements ISystemPluginListener {

    public static AppApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SystemPluginManager.getInstance().setSystemPluginListener(this, this);
    }

    @Override
    public void onBatteryChange(int intLevel, int intScale) {
        Log.d("AppApplication", "onBatteryChange:" + intLevel + "/" + intScale);

    }

    @Override
    public void onScreenChange(boolean isOff) {
        Log.d("AppApplication", "onScreenChange:" + isOff);
    }
}
