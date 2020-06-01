package com.mango.loadlibtool;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class InjectTool {

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
     *
     * return : 有值为错误原因 null代表成功
     * */
    public static String inject(final Context context, final String targetPackageName, final String dexName, final String className, final String methodName) {

        Log.d("hhz", "1");
        if (!CommandTool.hasRoot()) return "未获取Root权限";
        Log.d("hhz", "2");
        if (!hasStoragePermission(context)) return "没有写权限";
        Log.d("hhz", "3");
        if (!copyAllFiles(context, dexName)) return "写入文件失败";
        Log.d("hhz", "4");
        int runningResult = CommandTool.execRootCmdSilent(getTmpStorePath() + "exelib "
                + "isAppRunning "
                + targetPackageName);
        if (runningResult <= 0) return "目标程序未运行";
        Log.d("hhz", "5");

        new Thread(new Runnable() {
            @Override
            public void run() {
                CommandTool.execRootCmdSilent(getTmpStorePath() + "exelib "
                        + "inject "
                        + targetPackageName + " "
                        + getTmpStorePath() + "libloaddex.so " + " "
                        + getTmpStorePath() + dexName + " "
                        + className + " "
                        + methodName);
                Log.d("hhz", "7");
            }
        }).start();
        Log.d("hhz", "6");

        return null;
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
