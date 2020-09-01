package com.mango.puppet.systemplugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.mango.loadlibtool.CommandTool;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.systemplugin.i.ISystemPluginExecute;
import com.mango.puppet.systemplugin.i.ISystemPluginListener;
import com.mango.puppet.systemplugin.i.ISystemPluginQuery;

import java.io.File;
import java.util.List;

/**
 * SystemPluginManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class SystemPluginManager implements ISystemPluginExecute, ISystemPluginQuery, ISystemPluginListener {
    private static final SystemPluginManager ourInstance = new SystemPluginManager();

    public static SystemPluginManager getInstance() {
        return ourInstance;
    }

    private SystemPluginManager() {
    }

    /************   public   ************/
    public void setSystemPluginListener(Context context) {
        Intent intent = new Intent(context, SystemService.class);
        context.startService(intent);
    }

    /************   ISystemPluginExecute   ************/
    @Override
    public int execRootCmd(String commandString) {
        LogManager.getInstance().recordDebugLog("root权限执行命令行" + commandString);
        return CommandTool.execRootCmdSilent(commandString);
    }

    @Override
    public String execRootCmdWithResult(String commandString) {
        LogManager.getInstance().recordDebugLog("root权限执行命令行并输出结果" + commandString);
        return CommandTool.execRootCmd(commandString);
    }

    @Override
    public void changeForegroundApplication(String packageName, String activityName, ISystemPluginResult result) {
        LogManager.getInstance().recordDebugLog("更改前台应用" + packageName + activityName);
        if (!judgeRoot(result)) {
            return;
        }

        String current = getCurrentForegroundApplication();
        if (current != null && current.equals(packageName)) {
            if (result != null) {
                result.onFinished(true, "");
            }
        } else {
            startActivity(packageName, activityName);
            if (result != null) {
                current = getCurrentForegroundApplication();
                boolean flag = !TextUtils.isEmpty(current) && current.equals(packageName);
                result.onFinished(flag, flag ? "" : "重启失败");
            }
        }
    }

    @Override
    public void installApplication(String apkPath, ISystemPluginResult result) {
        LogManager.getInstance().recordDebugLog("安装应用" + apkPath);
        if (!judgeRoot(result)) {
            return;
        }

        if (!new File(apkPath).exists()) {
            if (result != null) {
                result.onFinished(false, "文件不存在");
            }
        } else {
            execRootCmd("pm install " + apkPath);
            if (result != null) {
                result.onFinished(true, "");
            }
        }
    }

    @Override
    public void uninstallApplication(final String packageName, ISystemPluginResult result) {
        LogManager.getInstance().recordDebugLog("卸载应用" + packageName);
        if (!judgeRoot(result)) {
            return;
        }

        execRootCmd("pm uninstall --user 0 " + packageName);
        if (result != null) {
            result.onFinished(true, "");
        }
    }

    @Override
    public void exitApplication(final String packageName, final ISystemPluginResult result) {
        LogManager.getInstance().recordDebugLog("退出应用" + packageName);
        if (!judgeRoot(result)) {
            return;
        }

        execRootCmd("am force-stop " + packageName);
        if (result != null) {
            result.onFinished(true, "");
        }
    }

    @Override
    public void restartApplication(String packageName, String activityName, ISystemPluginResult result) {
        LogManager.getInstance().recordDebugLog("重启应用" + packageName);
        if (!judgeRoot(result)) {
            return;
        }

        execRootCmd("am force-stop " + packageName);
        startActivity(packageName, activityName);
        if (result != null) {
            String current = getCurrentForegroundApplication();
            boolean flag = !TextUtils.isEmpty(current) && current.equals(packageName);
            result.onFinished(flag, flag ? "" : "重启失败");
        }
    }

    /************   ISystemPluginQuery   ************/
    @Override
    public String getCurrentForegroundApplication() {
        LogManager.getInstance().recordDebugLog("获取当前前台应用");
        if (!judgeRoot(null)) {
            return null;
        }

        String ret = execRootCmdWithResult("dumpsys window windows | grep mFocusedApp");
        if (ret.contains("u0 ")) {
            ret = ret.substring(ret.indexOf("u0 ") + 3);
            if (ret.contains("/")) {
                return ret.substring(0, ret.indexOf("/"));
            }
        }
        return null;
    }

    @Override
    public boolean hasRootPermission() {
        LogManager.getInstance().recordDebugLog("检测手机是否root");
        return CommandTool.hasRoot();
    }

    @Override
    public String getApplicationVersion(Context context, String packageName) {
        LogManager.getInstance().recordDebugLog("获取目标应用版本号");
        if (context == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        String version = null;
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (packageName.equals(packageInfo.packageName)) {
                version = packageInfo.versionName;
                break;
            }
        }
        return version;
    }

    /************   private   ************/
    private void startActivity(String packageName, String activityName) {
        String s = "am start -n '" + packageName + "/" + packageName + "." + activityName + "' -a android.intent.action.MAIN -c android.intent.category.LAUNCHER";
        execRootCmd(s);
    }

    private boolean judgeRoot(ISystemPluginResult result) {
        boolean flag = hasRootPermission();
        if (!flag) {
            if (result != null) {
                result.onFinished(false, "未获取root权限");
            }
        }
        return flag;
    }

    @Override
    public void onBatteryChange(int intLevel, int intScale) {

    }

    @Override
    public void onScreenChange(boolean isOff) {

    }
}
