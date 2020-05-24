package com.mango.puppet.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.mango.loadlibtool.InjectTool;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.plugin.i.IPluginEvent;
import com.mango.puppet.plugin.i.IPluginJob;
import com.mango.puppet.plugin.i.IPluginRunListener;
import com.mango.puppet.systemplugin.SystemPluginManager;
import com.mango.puppet.systemplugin.i.ISystemPluginExecute;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.mango.transmit.TransmitManager;
import com.mango.transmit.i.ITransmitReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * PluginManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class PluginManager implements IPluginControl, IPluginJob, IPluginEvent, ITransmitReceiver {
    private static final PluginManager ourInstance = new PluginManager();
    private IPluginRunListener listener = null;
    private static final String CLASS_NAME = "";
    private static final String METHOD_NAME = "";
    private List<PluginModel> models = new ArrayList<>();
    private Map<String, IPluginControlResult> runListenerMap;
    private ArrayList<String> runningPackageNames = new ArrayList<>();
    private ArrayList<String> pluginPackageNames = new ArrayList<>();
    private Context context;

    public static PluginManager getInstance() {
        return ourInstance;
    }

    private PluginManager() {
    }

    /************   public   ************/
    public void setPluginControlListener(IPluginRunListener listener) {
        this.listener = listener;
    }

    /************   IPluginControl   ************/
    @Override
    public void runPuppetPlugin(Context context, String targetPackageName, String dexName, String className, String methodName, IPluginControlResult result) {
        InjectTool.inject(context, targetPackageName, dexName, className, methodName);
    }

    /**
     * 获取可使用的插件 注:需已经安装目标app且版本正确
     */
    @Override
    public ArrayList<String> getSupportPuppetPlugin() {
        if (pluginPackageNames.size() > 0) {
            return pluginPackageNames;
        }
        if (models == null || models.size() == 0) {
            return new ArrayList<>();
        }

        for (PluginModel model : models) {
            String version = SystemPluginManager.getInstance().getApplicationVersion(context, model.getPackageName());
            if (version != null && version.equals(model.getDexVersion())) {
                pluginPackageNames.add(model.getPackageName());
            }

        }
        return pluginPackageNames;
    }

    @Override
    public ArrayList<String> getRunningPuppetPlugin() {
        return (ArrayList<String>) runningPackageNames;
    }

    @Override
    public void isPluginRunning(String packageName, IPluginControlResult result) {
        for (String runningPackageName : runningPackageNames) {
            if (packageName.equals(runningPackageName)) {
                result.onFinished(true, "");
                return;
            }
        }
        result.onFinished(false, "");
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == 1) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "heart");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String packageName = (String) msg.obj;
                TransmitManager.getInstance().sendMessage(packageName, jsonObject);
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        //检测心跳是否回应
                        runningPackageNames.remove(packageName);
                        for (PluginModel model : models) {
                            if (model.getPackageName().equals(packageName) && model.isRun()) {
                                runningPackageNames.add(packageName);
                            }
                        }
                        if (!runningPackageNames.contains(packageName) && listener != null) {
                            listener.onPluginRunningStatusChange(packageName, false);
                        } else if (runningPackageNames.contains(packageName) && listener != null) {
                            listener.onPluginRunningStatusChange(packageName, true);
                        }
                    }
                };
                timer.schedule(timerTask, 2000, 5000);
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 1 检查是否root及是否有读写权限
     * 2 根据插件管理层暴露的接口 检查目标app是否安装、破解插件是否存在、破解插件的版本和app版本是否一致
     * 3 启动数据传输模块
     * 4 运行木马程序
     */
    @Override
    public void startPluginSystem(Context context, final List<PluginModel> pluginModels, final IPluginControlResult result) {
        models = pluginModels;
        this.context = context;
        final boolean[] callBack = {false};
        if (!SystemPluginManager.getInstance().hasRootPermission()) {
            result.onFinished(false, "未开启Root权限");
        } else {
            try {
                //检测是否有写的权限
                int permission = ActivityCompat.checkSelfPermission(context,
                        "android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    result.onFinished(false, "未开启读写权限");
                } else {
                    if (getSupportPuppetPlugin().size() == 0) {
                        result.onFinished(false, "无支持插件");
                    } else {
                        TransmitManager.getInstance().setTransmitReceiver(this);
                        final int[] i = {0};
                        for (final PluginModel pluginModel : pluginModels) {
                            if (pluginPackageNames.contains(pluginModel.getPackageName())) {
                                runPuppetPlugin(context, pluginModel.getPackageName(), pluginModel.getDexPath(), CLASS_NAME, METHOD_NAME, new IPluginControlResult() {
                                    @Override
                                    public void onFinished(boolean isSucceed, String failReason) {
                                        sendHeart(pluginModel.getPackageName());
                                        if (isSucceed) {
                                            i[0]++;
                                            runningPackageNames.add(pluginModel.getPackageName());
                                        }
                                        if (listener != null) {
                                            listener.onPluginRunningStatusChange(pluginModel.getPackageName(), isSucceed);
                                        }
                                        if (!callBack[0] && !isSucceed) {
                                            result.onFinished(false, failReason);
                                            callBack[0] = true;
                                        }
                                        if (i[0] == pluginModels.size() && !callBack[0]) {
                                            result.onFinished(true, "");
                                        }
                                    }
                                });
                            } else {
                                if (!callBack[0]) {
                                    result.onFinished(false, pluginModel.getPackageName() + "插件不可用");
                                    callBack[0] = true;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.onFinished(false, e.getMessage());
            }
        }
    }

    private void sendHeart(final String packageName) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                message.obj = packageName;
                handler.sendMessage(message);
            }
        };

        timer.schedule(timerTask, 0, 5000);
    }

    /************   IPluginEvent   ************/
    @Override
    public void distributeEventWatcher(EventWatcher eventWatcher, IPluginControlResult result) {
        runListenerMap.put(eventWatcher.event_name, result);
        TransmitManager.getInstance().sendEventWatcher(eventWatcher.package_name, eventWatcher);
    }

    @Override
    public void distributeEvent(Event event, IPluginControlResult result) {
        runListenerMap.put(event.event_name, result);
        TransmitManager.getInstance().sendEvent(event.package_name, event);
    }

    /************   IPluginJob   ************/
    @Override
    public void distributeJob(final Job job, final IPluginControlResult result) {
        String activityName = "";
        for (PluginModel model : models) {
            if (model.getPackageName().equals(job.package_name))
                activityName = model.getActivityName();
        }
        SystemPluginManager.getInstance().changeForegroundApplication(job.package_name, activityName, new ISystemPluginExecute.ISystemPluginResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                if (isSucceed) {
                    runListenerMap.put(job.job_id + "", result);
                    TransmitManager.getInstance().sendJob(job.package_name, job);
                } else {
                    result.onFinished(isSucceed, failReason);
                }
            }
        });
    }

    @Override
    public void onReceiveJob(String packageName, Job job) {
        IPluginControlResult controlResult = runListenerMap.get(job.job_id + "");
        if (controlResult != null) {
            controlResult.onFinished(job.error_code == 0, job.error_message);
        }
        runListenerMap.remove(job.job_id + "");
    }

    @Override
    public void onReceiveEvent(String packageName, Event event) {
        IPluginControlResult controlResult = runListenerMap.get(event.event_name);
        if (controlResult != null) {
            controlResult.onFinished(true, "");
        }
        runListenerMap.remove(event.event_name);
    }

    @Override
    public void onReceiveEventWatcher(String packageName, EventWatcher eventWatcher) {
        IPluginControlResult controlResult = runListenerMap.get(eventWatcher.event_name);
        if (controlResult != null) {
            controlResult.onFinished(true, "");
        }
        runListenerMap.remove(eventWatcher.event_name);
    }

    @Override
    public void onReceiveEventData(String packageName, String dataString) {

    }

    @Override
    public void onReceiveEventData(String packageName, JSONObject jsonObject) {
        try {
            String heart = jsonObject.getString("type");
            if ("heart".equals(heart)) {
                for (PluginModel model : models) {
                    if (packageName.equals(model.getPackageName())) {
                        model.setRun(true);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
