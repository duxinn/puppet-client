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
    public void runPuppetPlugin(Context context, String targetPackageName, String dexName, String className, String methodName, IPluginControlResult result) {
//        InjectTool.inject(context, targetPackageName, dexName, className, methodName);
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

    /**
     * 1 检查是否root及是否有读写权限
     * 2 根据插件管理层暴露的接口 检查目标app是否安装、破解插件是否存在、破解插件的版本和app版本是否一致
     * 3 启动数据传输模块
     * 4 运行木马程序
     */
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
