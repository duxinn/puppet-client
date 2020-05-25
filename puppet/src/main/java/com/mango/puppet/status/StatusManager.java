package com.mango.puppet.status;

import com.mango.puppet.status.i.IStatusControl;
import com.mango.puppet.status.i.IStatusListener;

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

    private static final StatusManager ourInstance = new StatusManager();

    public static StatusManager getInstance() {
        return ourInstance;
    }

    private StatusManager() {
    }

    /************   public   ************/
    public void setStatusListener(IStatusListener listener) {

    }

    /************   ILog   ************/
    @Override
    public void setNetworkStatus(int status) {

    }

    @Override
    public int getNetworkStatus() {
        return 0;
    }

    @Override
    public void setJobEngineStatus(int status) {

    }

    @Override
    public int getJobEngineStatus() {
        return 0;
    }

    @Override
    public void setJobCount(int count) {

    }

    @Override
    public int getJobCount() {
        return 0;
    }

    @Override
    public void setJobResultCount(int count) {

    }

    @Override
    public int getJobResultCount() {
        return 0;
    }

    @Override
    public void setEventWatcher(String packageName, String eventName, boolean isvalid) {

    }

    @Override
    public boolean getEventWatcher(String packageName, String eventName) {
        return false;
    }

    @Override
    public List<String> getApplicationEventWatcher(String packageName) {
        return null;
    }

    @Override
    public Map<String, List<String>> getAllEventWatcher() {
        return null;
    }

    @Override
    public boolean isPluginRunning(String packageName) {
        return false;
    }

    @Override
    public List<String> getAllRunningPlugin() {
        return null;
    }

    @Override
    public void setPluginRunning(String packageName, boolean isRunning) {

    }
}
