package com.mango.puppet.dispatch.system;

import android.content.Context;

import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.system.i.ISystem;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.plugin.i.IPluginRunListener;
import com.mango.puppet.status.StatusManager;

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
    public void startSystem(final Context context) {
        // 1 插件管理模块
        PluginManager.getInstance().startPluginSystem(context, new IPluginControl.IPluginControlResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                if (!isSucceed) {
                    LogManager.getInstance().recordLog("Plugin启动失败");
                    LogManager.getInstance().recordLog(failReason);
                } else  {
                    LogManager.getInstance().recordLog("Plugin启动成功");
                    PluginManager.getInstance().setPluginControlListener(SystemManager.this);
                    // 2 任务模块
                    boolean result = JobManager.getInstance().startJobSystem(context);
                    if (!result) {
                        LogManager.getInstance().recordLog("Job启动失败");
                    } else {
                        LogManager.getInstance().recordLog("Job启动成功");
                        // 3 事件模块
                        result = EventManager.getInstance().startEventSystem(context);
                        if (!result) {
                            LogManager.getInstance().recordLog("Event启动失败");
                        } else {
                            LogManager.getInstance().recordLog("Event启动成功");
                            // 4 网络模块
                            NetworkManager.getInstance().setupNetwork(context, new INetwork.ISetupResult() {
                                @Override
                                public void onSuccess() {
                                    LogManager.getInstance().recordLog("Network启动成功");
                                    LogManager.getInstance().recordLog("Puppet启动成功");
                                }

                                @Override
                                public void onFailure() {
                                    LogManager.getInstance().recordLog("Network启动失败");
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /************   IPluginRunListener   ************/
    @Override
    public void onPluginRunningStatusChange(String packageName, boolean isRunning) {
        StatusManager.getInstance().setPluginRunning(packageName, isRunning);
    }
}
