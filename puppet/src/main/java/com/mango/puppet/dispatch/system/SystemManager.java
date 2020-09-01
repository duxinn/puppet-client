package com.mango.puppet.dispatch.system;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mango.puppet.bean.PluginModel;
import com.mango.puppet.config.PuppetConfig;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.system.i.ISystem;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.network.wsmanager.WsManager;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.plugin.i.IPluginRunListener;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.systemplugin.SystemPluginManager;
import com.mango.puppet.tool.DeviceIdTool;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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

    private Context context = null;
    private String deviceId = null;

    private Timer timer = null;
    private TimerTask timerTask = null;

    private SystemManager() {
    }

    /************   public   ************/
    public Context getContext() {
        return context;
    }

    public String getDeviceId() {
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        } else if (context != null){
            deviceId = DeviceIdTool.getDeviceId(context);
            return deviceId;
        }
        return null;
    }

    /************   ISystem   ************/
    @Override
    public void startSystem(final Context context) {
        destroyTimer();
        this.context = context;
        deviceId = DeviceIdTool.getDeviceId(context);
        LogManager.init(context);
        LogManager.getInstance().recordDebugLog("开始启动程序");
        SystemPluginManager.getInstance().setSystemPluginListener(context);
        // 1 插件管理模块
        //TODO pluginmodels列表暂时没有
        PluginModel wechatModel = new PluginModel();
        wechatModel.setPackageName("com.tencent.mm");
        wechatModel.setActivityName("ui.LauncherUI");
        wechatModel.setDexName(PuppetConfig.WECHAT_APK_NAME);
        wechatModel.setClassName("com.mango.wechatplugin.WechatEntrance");
        wechatModel.setMethodName("entranceRoot");
        wechatModel.setDexVersion("7.0.16");
        ArrayList<PluginModel> models = new ArrayList<>();
        models.add(wechatModel);
        PluginManager.getInstance().setPluginControlListener(SystemManager.this);
        PluginManager.getInstance().startPluginSystem(context, models,new IPluginControl.IPluginControlResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                if (!isSucceed) {
                    LogManager.getInstance().recordLog("Plugin启动失败");
                    LogManager.getInstance().recordLog(failReason);
                } else  {
                    LogManager.getInstance().recordLog("Plugin启动成功");
                    NetworkManager.getInstance().setupApi();
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
                            NetworkManager.getInstance().setupNetwork(context, PuppetConfig.IS_LOCAL_SERVER, new INetwork.ISetupResult() {
                                @Override
                                public void onSuccess() {
                                    LogManager.getInstance().recordLog("Network启动成功");
                                    LogManager.getInstance().recordLog("Puppet启动成功");

                                    createTimer();

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
        LogManager.getInstance().recordLog(packageName + "木马插件运行状态变化为" + isRunning);
        Log.d(getClass().toString(), "onPluginRunningStatusChange:" + packageName + isRunning);
        StatusManager.getInstance().setPluginRunning(packageName, isRunning);
    }

    private void destroyTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void createTimer() {
        destroyTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                boolean isConnect = WsManager.isNetworkConnected(context);
                String s = "";
                if (isConnect) {
                    s += "网络正常 ";
                } else {
                    s += "网络已断开 ";
                }
                LogManager.getInstance().recordLog(s);
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 5000, 60 * 10 * 1000);
    }
}
