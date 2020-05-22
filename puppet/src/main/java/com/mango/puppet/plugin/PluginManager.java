package com.mango.puppet.plugin;

import android.content.Context;

import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.plugin.i.IPluginEvent;
import com.mango.puppet.plugin.i.IPluginJob;
import com.mango.puppet.plugin.i.IPluginRunListener;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;

import java.util.ArrayList;

/**
 * PluginManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class PluginManager implements IPluginControl, IPluginJob, IPluginEvent {
    private static final PluginManager ourInstance = new PluginManager();

    public static PluginManager getInstance() {
        return ourInstance;
    }

    private PluginManager() {
    }

    /************   public   ************/
    public void setPluginControlListener(IPluginRunListener listener) {

    }

    /************   IPluginControl   ************/
    @Override
    public void runPuppetPlugin(Context context, String targetPackageName, String className, String methodName, IPluginControlResult result) {

    }

    @Override
    public ArrayList<String> getSupportPuppetPlugin() {
        return null;
    }

    @Override
    public ArrayList<String> getRunningPuppetPlugin() {
        return null;
    }

    @Override
    public void isPluginRunning(String packageName, IPluginControlResult result) {

    }

    @Override
    public void startPluginSystem(Context context, IPluginControlResult result) {

    }

    /************   IPluginEvent   ************/
    @Override
    public void distributeEventWatcher(EventWatcher eventWatcher, IPluginControlResult result) {

    }

    @Override
    public void distributeEvent(Event event, IPluginControlResult result) {

    }

    /************   IPluginJob   ************/
    @Override
    public void distributeJob(Job job, IPluginControlResult result) {

    }
}
