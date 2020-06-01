package com.mango.puppet.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.mango.loadlibtool.InjectTool;
import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    private ArrayList<String> runningPackageNames = new ArrayList<>();
    private ArrayList<String> pluginPackageNames = new ArrayList<>();
    private Context context;
    private boolean callBack = false;
    private IPluginControlResult iPluginControlResult;
    private Timer timer;

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
        return runningPackageNames;
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
                    jsonObject.put(TransmitManager.TYPE_KEY, TransmitManager.HEART_KEY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (PluginModel model : models) {
                    model.setRun(false);
                    TransmitManager.getInstance().sendMessage(model.getPackageName(), jsonObject);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (PluginModel model : models) {
                            if (model.isRun()) {
                                if (!runningPackageNames.contains(model.getPackageName())){
                                    runningPackageNames.add(model.getPackageName());
                                    if (listener != null) {
                                        listener.onPluginRunningStatusChange(model.getPackageName(), true);
                                    }
                                }
                            } else {
                                if (runningPackageNames.contains(model.getPackageName())){
                                    runningPackageNames.remove(model.getPackageName());
                                    if (listener != null) {
                                        listener.onPluginRunningStatusChange(model.getPackageName(), false);
                                    }
                                }
                            }
                        }
                    }
                }, 3000);
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
        if (iPluginControlResult!=null){
            result.onFinished(false,"插件正在启动中,请勿重复调用");
            return;
        }
        callBack = false;
        models = pluginModels;
        this.context = context;
        iPluginControlResult = result;
        if (!SystemPluginManager.getInstance().hasRootPermission()) {
            result.onFinished(false, "未开启Root权限");
            iPluginControlResult=null;
        } else {
            try {
                //检测是否有写的权限
                int permission = ActivityCompat.checkSelfPermission(context,
                        "android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    result.onFinished(false, "未开启读写权限");
                    iPluginControlResult=null;
                } else {
                    if (getSupportPuppetPlugin().size() == 0) {
                        result.onFinished(false, "无支持插件");
                        iPluginControlResult=null;
                    } else {
                        TransmitManager.getInstance().setTransmitReceiver(this);
                        ArrayList<String> actions = new ArrayList<>();
                        actions.add(TransmitManager.MANAGER_PACKAGE_NAME);
                        TransmitManager.getInstance().setRegister(context, actions);
                        for (final PluginModel pluginModel : pluginModels) {
                            if (!pluginPackageNames.contains(pluginModel.getPackageName())) {
                                result.onFinished(false, pluginModel.getPackageName() + "插件不可用");
                                iPluginControlResult=null;
                                callBack = true;
                                return;
                            }
                        }
                        for (final PluginModel pluginModel : pluginModels) {
                            runPuppetPlugin(context, pluginModel.getPackageName(), pluginModel.getDexName(), pluginModel.getClassName(), pluginModel.getMethodName(), result);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!callBack) {
                                    result.onFinished(false, "启动插件超时");
                                    iPluginControlResult=null;
                                    callBack = true;
                                }
                            }
                        }, 5000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.onFinished(false, e.getMessage());
                iPluginControlResult=null;
            }
        }
    }

    private void sendHeart() {
        if (timer == null) {
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
            timer.schedule(timerTask, 0, 5000);
        }
    }

    /************   IPluginEvent   ************/
    @Override
    public void distributeEventWatcher(EventWatcher eventWatcher, IPluginControlResult result) {
        if (runningPackageNames.contains(eventWatcher.package_name)) {
            result.onFinished(true, "");
            TransmitManager.getInstance().sendEventWatcher(eventWatcher.package_name, eventWatcher);
        } else {
            result.onFinished(false, "插件未运行");
        }
    }

    @Override
    public void distributeEvent(Event event, IPluginControlResult result) {
        if (runningPackageNames.contains(event.package_name)) {
            result.onFinished(true, "");
            TransmitManager.getInstance().sendEvent(event.package_name, event);
        } else {
            result.onFinished(false, "插件未运行");
        }
    }

    /************   IPluginJob   ************/
    @Override
    public void distributeJob(final Job job, final IPluginJobCallBack result) {
        String activityName = "";
        for (PluginModel model : models) {
            if (model.getPackageName().equals(job.package_name))
                activityName = model.getActivityName();
        }
        SystemPluginManager.getInstance().changeForegroundApplication(job.package_name, activityName, new ISystemPluginExecute.ISystemPluginResult() {
            @Override
            public void onFinished(boolean isSucceed, String failReason) {
                if (isSucceed) {
                    if (runningPackageNames.contains(job.package_name)) {
                        result.onFinished(job,true, "");
                        TransmitManager.getInstance().sendJob(job.package_name, job);
                    } else {
                        result.onFinished(job,false, "插件未运行");
                    }
                } else {
                    result.onFinished(job, isSucceed, failReason);
                }
            }
        });
    }

    @Override
    public void onReceiveJob(String packageName, Job job) {
        JobManager.getInstance().receiveJobResult(job);
    }

    @Override
    public void onReceiveEvent(String packageName, Event event) {
        EventManager.getInstance().uploadNewEvent(event);
    }

    @Override
    public void onReceiveEventWatcher(String packageName, EventWatcher eventWatcher) {
    }

    @Override
    public void onReceiveData(final String packageName, JSONObject jsonObject) {
        try {
            String type = jsonObject.getString(TransmitManager.TYPE_KEY);
            if (TransmitManager.HEART_KEY.equals(type)) {
                for (PluginModel model : models) {
                    if (packageName.equals(model.getPackageName())) {
                        model.setRun(true);
                    }
                }
            } else if (TransmitManager.FIRST_START_KEY.equals(type)) {
                runningPackageNames.add(packageName);
                if (listener != null) {
                    listener.onPluginRunningStatusChange(packageName, true);
                }
                for (PluginModel model : models) {
                    if (packageName.equals(model.getPackageName())) {
                        model.setRun(true);
                    }
                }
                if (runningPackageNames.size() == models.size() && !callBack) {
                    iPluginControlResult.onFinished(true, "");
                    iPluginControlResult=null;
                    sendHeart();
                    callBack = true;
                }
            } else if (TransmitManager.UPLOAD_KEY.equals(type)) {
                final String filePath = jsonObject.getString(TransmitManager.LOCAL_URL_KEY);
                final String fileName = jsonObject.getString(TransmitManager.FILE_NAME);
                if (new File(filePath).exists()) {
                    UploadImageUtils.uploadImage(0x00, fileName, filePath, new UploadImageUtils.UploadImageCallBack() {
                        @Override
                        public void uploadImageSuccess(int requestCode, String identifier, String urlPath) {
                            try {
                                JSONObject receiptObject = new JSONObject();
                                receiptObject.put(TransmitManager.TYPE_KEY, TransmitManager.UPLOAD_KEY);
                                receiptObject.put(TransmitManager.LOCAL_URL_KEY, filePath);
                                receiptObject.put(TransmitManager.FILE_NAME, fileName);
                                receiptObject.put(TransmitManager.REMOTE_URL_KEY, urlPath);
                                TransmitManager.getInstance().sendMessage(packageName, receiptObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void uploadImageFailed(int requestCode, String identifier) {
                            try {
                                JSONObject receiptObject = new JSONObject();
                                receiptObject.put(TransmitManager.TYPE_KEY, TransmitManager.UPLOAD_KEY);
                                receiptObject.put(TransmitManager.LOCAL_URL_KEY, filePath);
                                receiptObject.put(TransmitManager.FILE_NAME, fileName);
                                TransmitManager.getInstance().sendMessage(packageName, receiptObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void uploadImageProgress(int requestCode, String identifier, double percent) {

                        }
                    });
                } else {
                    JSONObject receiptObject = new JSONObject();
                    receiptObject.put(TransmitManager.TYPE_KEY, TransmitManager.UPLOAD_KEY);
                    receiptObject.put(TransmitManager.LOCAL_URL_KEY, filePath);
                    receiptObject.put(TransmitManager.FILE_NAME, fileName);
                    TransmitManager.getInstance().sendMessage(packageName, receiptObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveDataString(String packageName, String dataString) {

    }
}
