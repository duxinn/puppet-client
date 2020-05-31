package com.mango.puppet.status;

import android.util.Log;

import com.mango.puppet.log.LogManager;
import com.mango.puppet.status.i.IStatusControl;
import com.mango.puppet.status.i.IStatusListener;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.PluginRunningModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StatusManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class StatusManager implements IStatusControl {
    public static final int SERVER_START = 0;
    public static final int SERVER_STOP = 1;
    public static final int SERVER_ERROR = 2;

    private static IStatusListener mListener;
    int networkStatus = -99;
    int jobEngineStatus = -99;
    int jobCount = 0;
    int jobResultCount = 0;
    List<EventWatcher> eventWatchModelList = new ArrayList<>();
    List<PluginRunningModel> pluginRunningModelList = new ArrayList<>();
//    List<HashMap<String, Object>> evenWatchList = new ArrayList<>();
//    List<HashMap<String, Boolean>> pluginRunningList = new ArrayList<>();

    private static final StatusManager ourInstance = new StatusManager();

    public static StatusManager getInstance() {
        return ourInstance;
    }

    private StatusManager() {
    }

    /************   public   ************/
    public void setStatusListener(IStatusListener listener) {
        mListener = listener;
    }

    /************   ILog   ************/
    @Override
    public void setNetworkStatus(int status) {
        boolean isNetOk;
        if (networkStatus != status) {
            networkStatus = status;
            if (networkStatus == 0) {
                isNetOk = true;
            } else {
                isNetOk = false;
            }
            if (mListener != null) {
                mListener.onNetworkStatusChanged(isNetOk);
            }
        }
    }

    @Override
    public int getNetworkStatus() {
        return networkStatus;
    }

    @Override
    public void setJobEngineStatus(int status) {
        if (jobEngineStatus != status) {
            jobEngineStatus = status;
            if (mListener != null) {
                mListener.onJobEngineStatusChanged(jobEngineStatus);
            }
        }
    }

    @Override
    public int getJobEngineStatus() {
        return jobEngineStatus;
    }

    @Override
    public void setJobCount(int count) {
//        jobCount = count;
//        if (mListener != null) {
//            mListener.onJobCountChanged(count);
//        }
        if (jobCount != count) {
            jobCount = count;
            if (mListener != null) {
                mListener.onJobEngineStatusChanged(jobCount);
            }
        }
    }

    @Override
    public int getJobCount() {
        return jobCount;
    }

    @Override
    public void setJobResultCount(int count) {
//        jobResultCount = count;
//        if (mListener != null){
//            mListener.onJobResultCountChanged(count);
//        }
        if (jobResultCount != count) {
            jobResultCount = count;
            if (mListener != null) {
                mListener.onJobEngineStatusChanged(jobResultCount);
            }
        }
    }

    @Override
    public int getJobResultCount() {
        return jobResultCount;
    }

    @Override
    public void setEventWatcher(String packageName, String eventName, boolean isvalid) {
//        HashMap<String, Object> eventWatchModel = new HashMap();
        EventWatcher eventWatchModel = new EventWatcher();
        Boolean isNeed = true;
        int watchStatus;
        if (isvalid){
            watchStatus =1;
        }else {
            watchStatus = 0;
        }
        if (eventWatchModelList.size() > 0) {
            for (int i = 0; i < eventWatchModelList.size(); i++) {
                EventWatcher model = eventWatchModelList.get(i);
                String packageNameIn = model.package_name;
                if (packageName.equals(packageNameIn)) {
                    String eventNameIn = model.event_name;
                    if (eventName.equals(eventNameIn)) {
                        int isValidIn = model.watcher_status;
                        //发生改变修改原来model的值
                        if (isValidIn != watchStatus) {
                            model.watcher_status = watchStatus;
                            if (mListener != null) {
                                mListener.onEventWatcherChanged();
                            }
                        }
                        isNeed = false;
                    }
                }
            }
        }
        //向list中添加一个新的model
        if (isNeed) {
            eventWatchModel.package_name = packageName;
            eventWatchModel.event_name = eventName;
            eventWatchModel.watcher_status = watchStatus;
            eventWatchModelList.add(eventWatchModel);
            if (mListener != null) {
                mListener.onEventWatcherChanged();
            }
        }
    }

    @Override
    public boolean getEventWatcher(String packageName, String eventName) {
        boolean isValid = false;
        if (eventWatchModelList.size() > 0) {
            for (int i = 0; i < eventWatchModelList.size(); i++) {
                EventWatcher model = eventWatchModelList.get(i);
                String packageNameIn = model.package_name;
                String eventNameIn = model.event_name;
                if (packageName.equals(packageNameIn) && eventName.equals(eventNameIn)) {
                    if ((model.watcher_status == 1)){
                        isValid = true;
                    }
                    return isValid;
                }
            }
        }
        return isValid;
    }

    @Override
    public List<String> getApplicationEventWatcher(String packageName) {
        List<String> packageNameList = new ArrayList<>();
        if (eventWatchModelList.size() > 0) {
            for (int i = 0; i < eventWatchModelList.size(); i++) {
                if (eventWatchModelList.get(i).watcher_status == 1 && packageName.equals(eventWatchModelList.get(i).package_name)) {
                    packageNameList.add(eventWatchModelList.get(i).event_name);
                }
            }
        }
        return packageNameList;
    }

    //u
    @Override
    public Map<String, List<String>> getAllEventWatcher() {
        Map<String, List<String>> eventMap = new HashMap<>();
        if (eventWatchModelList.size() > 0) {
            for (int i = 0; i < eventWatchModelList.size(); i++) {
                EventWatcher model = eventWatchModelList.get(i);
                if (model.watcher_status == 1) {
                    String packageNameIn = model.package_name;
                    String eventNameIn = model.event_name;
                    if (eventMap.get(packageNameIn) != null) {
                        eventMap.get(packageNameIn).add(eventNameIn);
                    } else {
                        List<String> eventNameList = new ArrayList<>();
                        eventNameList.add(eventNameIn);
                        eventMap.put(packageNameIn, eventNameList);
                    }
                }
            }
        }
        return eventMap;
    }

    @Override
    public boolean isPluginRunning(String packageName) {
        Boolean isRunning = false;
        if (pluginRunningModelList.size() > 0) {
            for (int i = 0; i < pluginRunningModelList.size(); i++) {
                if (packageName.equals(pluginRunningModelList.get(i).packageName)) {
                    isRunning = pluginRunningModelList.get(i).isRunning;
                }
            }
        }
        return isRunning;
    }

    @Override
    public List<String> getAllRunningPlugin() {
        List<String> runningPiuginList = new ArrayList<>();
        for (int i = 0; i < pluginRunningModelList.size(); i++) {
//            if (pluginRunningList.get(i).get)
            if (pluginRunningModelList.get(i).isRunning) {
                runningPiuginList.add(pluginRunningModelList.get(i).packageName);
            }
        }
        return runningPiuginList;
    }

    @Override
    public void setPluginRunning(String packageName, boolean isRunning) {
        LogManager.getInstance().recordLog("插件运行状态" + packageName + " " + isRunning);
        Boolean isNeed = true;
        for (int i = 0; i < pluginRunningModelList.size(); i++) {
            String packageNameIn = pluginRunningModelList.get(i).packageName;
            if (packageName.equals(packageNameIn)) {
                pluginRunningModelList.get(i).isRunning = isRunning;
                if (mListener != null) {
                    mListener.onPluginRunningChanged();
                }
                isNeed = false;
            }
        }
        if (isNeed) {
            PluginRunningModel pluginRunningModel = new PluginRunningModel();
            pluginRunningModel.packageName = packageName;
            pluginRunningModel.isRunning = isRunning;
            pluginRunningModelList.add(pluginRunningModel);
            if (mListener != null) {
                mListener.onPluginRunningChanged();
            }
        }
    }
}
