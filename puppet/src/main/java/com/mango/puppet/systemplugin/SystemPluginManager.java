package com.mango.puppet.systemplugin;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.mango.puppet.systemplugin.i.ISystemPluginExecute;
import com.mango.puppet.systemplugin.i.ISystemPluginListener;
import com.mango.puppet.systemplugin.i.ISystemPluginQuery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

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

    private ISystemPluginListener mListener;

    private SystemPluginManager() {
    }

    /************   public   ************/
    public void setSystemPluginListener(Application application, ISystemPluginListener listener) {
        mListener = listener;
        Intent intent = new Intent(application, SystemService.class);
        application.startService(intent);
    }

    /************   ISystemPluginExecute   ************/
    @Override
    public int execRootCmd(String commandString) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            dos.writeBytes(commandString + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public String execRootCmdWithResult(String commandString) {
        StringBuilder result = new StringBuilder();
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes(commandString + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line;
            while ((line = dis.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            if (!TextUtils.isEmpty(result.toString())) {
                result = new StringBuilder(result.substring(0, result.length() - 1));
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    @Override
    public void changeForegroundApplication(String packageName, String activityName, ISystemPluginResult result) {
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
        Boolean flag;
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "su";
            process = Runtime.getRuntime().exec(cmd);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public String getApplicationVersion(Context context, String packageName) {
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

    /************   public   ************/
    protected ISystemPluginListener getListener() {
        return mListener;
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
}
