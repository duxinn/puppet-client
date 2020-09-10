package com.mango.puppetsystem.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.mango.puppet.tool.ThreadPoolManager;
import com.mango.puppetsystem.AppApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkDownloadUtils {
    public final static String APK_DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + File.separator + "download/";

    /**
     * 从服务器中下载APK
     */
    public static void downLoadApk(final Context mContext, final String downURL) {
        final ProgressDialog pd;
        pd = new ProgressDialog(mContext);
        pd.setCancelable(true);// 下载中，可取消
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载安装包，请稍后");
        pd.setTitle("版本升级");
        pd.show();
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = downloadFile(downURL, mContext.getPackageName(), pd);
                    installApk(mContext, file);
                    // 结束掉进度条对话框
                    pd.dismiss();
                } catch (Exception e) {
                    pd.dismiss();
                }
            }
        });
    }

    /**
     * 从服务器下载最新更新文件
     *
     * @param path 下载路径
     * @param pd   进度条
     * @return
     * @throws Exception
     */
    private static File downloadFile(String path, String appName, ProgressDialog pd) throws Exception {
        // 如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            // 获取到文件的大小
            pd.setMax(conn.getContentLength());
            InputStream is = conn.getInputStream();
            String fileName = APK_DOWNLOAD_DIR
                    + appName + ".apk";
            File file = new File(fileName);
            // 目录不存在创建目录
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buffer = new byte[1024];
            int len;
            int total = 0;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
                // 获取当前下载量
                pd.setProgress(total);
            }
            fos.close();
            bis.close();
            is.close();
            return file;
        } else {
            throw new IOException("未发现有SD卡");
        }
    }

    /**
     * 安装apk
     */
    public static void installApk(Context mContext, File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            } else {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(AppApplication.instance, mContext.getPackageName() + ".fileprovider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            }
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
