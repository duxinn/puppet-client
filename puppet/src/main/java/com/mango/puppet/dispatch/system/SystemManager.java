package com.mango.puppet.dispatch.system;

import android.content.Context;

import com.mango.puppet.dispatch.system.i.ISystem;
import com.mango.puppet.plugin.i.IPluginRunListener;

/**
 * SystemManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class SystemManager implements ISystem, IPluginRunListener {
    private static final SystemManager ourInstance = new SystemManager();

    public static SystemManager getInstance() {
        return ourInstance;
    }

    private SystemManager() {
    }

    /************   ISystem   ************/
    @Override
    public void startSystem(Context context) {

    }

    /************   IPluginRunListener   ************/
    @Override
    public void onPluginRunningStatusChange(String packageName, boolean isRunning) {

    }
}
