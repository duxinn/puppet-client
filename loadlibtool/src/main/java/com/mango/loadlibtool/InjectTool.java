package com.mango.loadlibtool;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class InjectTool {

    public interface InjectResult {

        void injectFinished(boolean isSuccess, String failReason);
    }

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    /**
     * 注入到目标进程
     *
     * context : Context
     * targetPackageName: 目标app包名
     * className: 要在目标app中运行入口类名
     * methodName: 该类名下要调用的静态方法的方法名 注:必须是静态方法
     * activityName: 目标进程启动activity的名称
     * injectResult: 运行结果
     *
     * */
    public static void inject(final Context context,
                              final String targetPackageName,
                              final String dexName,
                              final String className,
                              final String methodName,
                              final String activityName,
                              final InjectResult injectResult) {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!CommandTool.hasRoot()) {
                    if (injectResult != null) {
                        injectResult.injectFinished(false, "未获取Root权限");
                    }
                    return;
                }
                if (!hasStoragePermission(context)) {
                    if (injectResult != null) {
                        injectResult.injectFinished(false, "没有写权限");
                    }
                    return;
                }
                if (!copyAllFiles(context, dexName)) {
                    if (injectResult != null) {
                        injectResult.injectFinished(false, "写入文件失败");
                    }
                    return;
                }
                injectDetail(targetPackageName, dexName, className, methodName, activityName, injectResult, 0);
            }
        });

    }

    private static String getCurrentForegroundApplication() {

        String ret = CommandTool.execRootCmd("dumpsys window windows | grep mFocusedApp");
        if (ret.contains("u0 ")) {
            ret = ret.substring(ret.indexOf("u0 ") + 3);
            if (ret.contains("/")) {
                return ret.substring(0, ret.indexOf("/"));
            }
        }
        return null;
    }

    private static void injectDetail(final String targetPackageName,
                                     final String dexName,
                                     final String className,
                                     final String methodName,
                                     final String activityName,
                                     final InjectResult injectResult,
                                     final int times) {
        long delay;
        if (times == 0 && targetPackageName.equals(getCurrentForegroundApplication())) {
            delay = 100L;
        } else {
            Log.d("gbinjectc" , "start app");
            CommandTool.execRootCmdSilent(CommandTool.stopApplicationCommand(targetPackageName));
            CommandTool.execRootCmdSilent(CommandTool.startApplicationCommand(targetPackageName, activityName));
            delay = 6000L;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                final int[] ret = {-1};
                final boolean[] runFinished = {false};

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ret[0] = CommandTool.execRootCmdSilent(getTmpStorePath() + "exelib "
                                + "inject "
                                + targetPackageName + " "
                                + getTmpStorePath() + "libloaddex.so " + " "
                                + getTmpStorePath() + dexName + " "
                                + className + " "
                                + methodName);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                runFinished[0] = true;
                                injectEnd(ret[0], targetPackageName, dexName, className, methodName, activityName, injectResult, times);
                            }
                        });
                    }
                }).start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!runFinished[0]) {
                            injectEnd(-2, targetPackageName, dexName, className, methodName, activityName, injectResult, times);
                        }
                    }
                }, 1000);
            }
        }, delay);
    }

    private static void injectEnd(int ret,
                                  final String targetPackageName,
                                  final String dexName,
                                  final String className,
                                  final String methodName,
                                  final String activityName,
                                  final InjectResult injectResult,
                                  final int times) {
        if (ret == 0) {
            if (injectResult != null) {
                injectResult.injectFinished(true, "");
            }
        } else {
            if (times >= 3) {
                if (injectResult != null) {
                    injectResult.injectFinished(false, "jni failed");
                }
            } else {
                injectDetail(targetPackageName,
                        dexName,
                        className,
                        methodName,
                        activityName,
                        injectResult,
                        times + 1);
            }
        }
    }

    private static boolean hasStoragePermission(Context context) {
        try {
            int permission = ActivityCompat.checkSelfPermission(context,
                    PERMISSIONS_STORAGE[1]);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean copyAllFiles(Context context, String dexName) {
        ArrayList<String> needCopyFileList = new ArrayList<>();
        needCopyFileList.add("exelib");
        needCopyFileList.add("libloaddex.so");
        needCopyFileList.add(dexName);
        if (copyFileDetail(needCopyFileList, context)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean copyFileDetail(ArrayList<String> needCopyFileList, Context context) {
        CommandTool.execRootCmdSilent("chmod 777 /data/local");

        // 不覆盖
        boolean existFlag = true;
        for (int i = 0; i < needCopyFileList.size(); i++) {
            if (!new File(getTmpStorePath() + needCopyFileList.get(i)).exists()) {
                existFlag = false;
                break;
            }
        }
//        if (existFlag) return true;

        boolean isSuccess = true;
        for (int i = 0; i < needCopyFileList.size(); i++) {
            if (!copyAssetAndWrite(needCopyFileList.get(i), context)) {
                isSuccess = false;
                break;
            }
        }
        if (!isSuccess) return isSuccess;

        for (int i = 0; i < needCopyFileList.size(); i++) {
            CommandTool.execRootCmdSilent("rm " + getTmpStorePath() + needCopyFileList.get(i));
        }

        for (int i = 0; i < needCopyFileList.size(); i++) {
            if (CommandTool.execRootCmdSilent("dd if=" + getStorePath() + needCopyFileList.get(i) + " of=" + getTmpStorePath() + needCopyFileList.get(i)) != 0
                    || CommandTool.execRootCmdSilent("chmod 777 " + getTmpStorePath() + needCopyFileList.get(i)) != 0) {
                isSuccess = false;
                break;
            }
        }
        return isSuccess;
    }

    private static boolean copyAssetAndWrite(String fileName, Context context) {
        try {
            File storeDir = new File(getStorePath());
            if (!storeDir.exists()) {
                storeDir.mkdirs();
            }
            File outFile = new File(getStorePath(), fileName);
            if (outFile.exists()) {
                outFile.delete();
            }
            InputStream is = context.getAssets().open("armeabi-v7a/" + fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getStorePath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + "loadlibfile/";
    }

    private static String getTmpStorePath() {
        return "/data/local/tmp/";
    }

    private static String getApkPath(String packageName) {
        String r = CommandTool.execRootCmd("ls /data/app");
        if (!TextUtils.isEmpty(r)) {
            String[] packages = r.split("\n");
            for (int i = 0; i < packages.length; i++) {
                if ((packages[i]).contains(packageName)) {
                    return packages[i];
                }
            }
        }
        return "";
    }
}
