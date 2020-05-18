package com.mango.puppet.systemplugin;

import com.mango.puppet.systemplugin.i.ISystemPluginExecute;
import com.mango.puppet.systemplugin.i.ISystemPluginListener;
import com.mango.puppet.systemplugin.i.ISystemPluginQuery;

/**
 * SystemPluginManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class SystemPluginManager implements ISystemPluginExecute, ISystemPluginQuery {
    private static final SystemPluginManager ourInstance = new SystemPluginManager();

    public static SystemPluginManager getInstance() {
        return ourInstance;
    }

    private SystemPluginManager() {
    }

    /************   public   ************/
    public void setSystemPluginListener(ISystemPluginListener listener) {

    }

    /************   ISystemPluginExecute   ************/
    @Override
    public int execRootCmd(String commandString) {
        return 0;
    }

    @Override
    public String execRootCmdWithResult(String commandString) {
        return null;
    }

    @Override
    public void changeForegroundApplication(String packageName, ISystemPluginResult result) {
        // TODO
        result.onFinished(true, "");
    }

    @Override
    public void installApplication(String apkPath, ISystemPluginResult result) {

    }

    @Override
    public void uninstallApplication(String packageName, ISystemPluginResult result) {

    }

    @Override
    public void exitApplication(String packageName, ISystemPluginResult result) {

    }

    @Override
    public void restartApplication(String packageName, ISystemPluginResult result) {

    }

    /************   ISystemPluginQuery   ************/
    @Override
    public String getCurrentForegroundApplication() {
        return null;
    }

    @Override
    public boolean hasRootPermission() {
        return false;
    }

    @Override
    public String getApplicationVersion(String packageName) {
        return null;
    }
}
