package com.mango.puppetsystem.update;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mango.puppet.tool.ThreadPoolManager;
import com.mango.puppetsystem.AppApplication;
import com.mango.puppetsystem.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {

    //fixme  地址为测试地址
    //正式地址格式如下: https://xxxxxx/api/xxxxxx/apkupdate?app=puppet
    private static final String APK_UPDATE_URL = "https://api.gbzc168.com/api/lottery_purchasing/apkupdate?app=wechatclient";

    private UpdateManager() {
    }

    private static UpdateManager sInstance = new UpdateManager();

    public static UpdateManager getInstance() {
        return sInstance;
    }


    public void doUpdate(final Activity activity) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //服务器返回的字段和Puppet系统不一致(msg和message字段)，所以用原生获取了。
                    URL url = new URL(APK_UPDATE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String respContent = readInputStreamToString(is);
                        if (respContent != null) {
                            checkAndUpdate(activity, new JSONObject(respContent));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void checkAndUpdate(final Activity activity, JSONObject jsonObject) {
        int status = jsonObject.optInt("status", -1);
        JSONObject dataJsonObject = jsonObject.optJSONObject("data");
        if (status == 0 && dataJsonObject != null) {
            final String downloadApkUrl = dataJsonObject.optString("downurl");
            final String version = dataJsonObject.optString("version");
            final String updateDesc = dataJsonObject.optString("app_desc");
            if (compareAppVersion(version, getVersionName()) > 0) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showUpdate(activity, updateDesc, String.format("是否升级到%s版本？", version), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //需要升级
                                    ApkDownloadUtils.downLoadApk(activity, downloadApkUrl);
                                }
                            });
                        }
                    });
                }
            }
        }
    }


    private static String readInputStreamToString(InputStream is) {
        byte[] result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            baos.close();
            result = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(result);
    }

    private static int compareAppVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return 0;
        }
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;
        while (idx < minLength
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0
                && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }

    private String getVersionName() {
        try {
            PackageManager packageManager = AppApplication.instance.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(AppApplication.instance.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "0.0";
    }

    public static void showUpdate(Context context, String description, final String content, final View.OnClickListener onClickListener) {
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_update);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dialog.dismiss();
            }
        };
        dialog.findViewById(R.id.tv_ok).setOnClickListener(listener);
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        TextView textView = dialog.findViewById(R.id.content);
        TextView tvDescription = dialog.findViewById(R.id.tv_description);
        textView.setText(content);
        if (!TextUtils.isEmpty(description)) {
            tvDescription.setText(description);
        }
        dialog.setCancelable(false);
        dialog.show();
    }
}
