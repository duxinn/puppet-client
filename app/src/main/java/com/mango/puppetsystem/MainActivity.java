package com.mango.puppetsystem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mango.loadlibtool.CommandTool;
import com.mango.puppet.bean.NormalConst;
import com.mango.puppet.config.PuppetConfig;
import com.mango.puppet.tool.PreferenceUtils;
import com.mango.puppetsystem.floatball.FloatBallService;
import com.mango.puppetsystem.floatball.FloatWindowService;
import com.mango.puppetsystem.update.UpdateManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.mango.puppet.network.wsmanager.WsManager.KEY_SOCKET_URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout retryLL;
    private LinearLayout copyLL;
    private ArrayList<String> logList = new ArrayList<>();
    private TextView tvLog, tvNet, tvJobCount, tvJobResultCount, tvJobEngineStatus, tvLocalStatus, tvEventWatcher;
    private EditText mEditWsUrl;
    private Button mBtnSetWsUrl;
    private Button mBtnQrCode;
    private MyReceiver myReceiver;
    private long mExitTime = 0;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.CAMERA"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        if (PuppetConfig.IS_NEED_ROOT) {
            boolean isRoot = CommandTool.hasRoot();
            if (!isRoot) {
                writeLog("请先开启ROOT权限");
            }
            copyLL.setVisibility(View.GONE);
        } else {
            copyLL.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initEvent();
            }
        }, 800);
    }

    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        retryLL = findViewById(R.id.retryTv);
        copyLL = findViewById(R.id.copyTv);
        mEditWsUrl = findViewById(R.id.edit_ws_url);
        mBtnSetWsUrl = findViewById(R.id.btn_set_ws_url);
        mBtnSetWsUrl.setOnClickListener(this);
        mBtnQrCode = findViewById(R.id.btn_qrcode);
        mBtnQrCode.setOnClickListener(this);
        String storeUrl = PreferenceUtils.getInstance().getString(KEY_SOCKET_URL, "");
        mEditWsUrl.setText(storeUrl);

        tvLog = findViewById(R.id.tvlog);
        tvNet = findViewById(R.id.tvNetStatus);
        tvJobCount = findViewById(R.id.tvJob);
        tvJobResultCount = findViewById(R.id.tvJobResult);
        tvJobEngineStatus = findViewById(R.id.tvJobEngineStatus);
        tvLocalStatus = findViewById(R.id.tvLocalStatus);
        tvEventWatcher = findViewById(R.id.tvEventWatcher);
        retryLL.setOnClickListener(this);
        copyLL.setOnClickListener(this);
    }

    private void initData() {
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, new IntentFilter(NormalConst.ACTION));

        //检查更新
        UpdateManager.getInstance().doUpdate(MainActivity.this);
    }

    private void initEvent() {
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        int permission1 = ActivityCompat.checkSelfPermission(this,
                "android.permission.READ_PHONE_STATE");
        int permission2 = ActivityCompat.checkSelfPermission(this,
                "android.permission.CAMERA");
        if (permission != PackageManager.PERMISSION_GRANTED
                || permission1 != PackageManager.PERMISSION_GRANTED
                || permission2 != PackageManager.PERMISSION_GRANTED) {
            verifyStoragePermissions(this);
        }
    }

    private void writeLog(final String content) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    writeLogDetail(content);
                }
            });
        } else {
            writeLogDetail(content);
        }

    }

    private void writeLogDetail(String content) {
        if (!TextUtils.isEmpty(content)) {
            if (logList.size() > 12) {
                logList.remove(0);
            }
            logList.add(content);
        }
        String string = "";
        for (String s : logList) {
            string += s;
            string += "\n";
        }
        string += "...";
        tvLog.setText(string);
    }

    private void startFloatBallService() {

        Intent stopIntent = new Intent(this, FloatBallService.class);
        stopService(stopIntent);
        stopIntent = new Intent(this, FloatWindowService.class);
        stopService(stopIntent);

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, FloatBallService.class);
                    startService(intent);

                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    startActivity(home);
                }
            }, 500);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.retryTv) {
            startFloatBallService();
        } else if (v.getId() == R.id.btn_set_ws_url) {
            if (!TextUtils.isEmpty(mEditWsUrl.getText().toString().trim())) {
                PreferenceUtils.getInstance().setString(KEY_SOCKET_URL, mEditWsUrl.getText().toString().trim());
                Toast.makeText(this, "设置成功即将关闭,请重启", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                }, 2500);
            }
        } else if (v.getId() == R.id.btn_qrcode) {
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.CAMERA");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                verifyStoragePermissions(this);
            } else {
                Intent intent = new Intent(this, QrCodeActivity.class);
                startActivityForResult(intent, 100);
            }
        } else if (v.getId() == R.id.copyTv) {
            copyFile();
        }
    }

    private void copyFile() {
        ArrayList<String> needCopyFileList = new ArrayList<>();
        needCopyFileList.add(PuppetConfig.WECHAT_APK_NAME);
        needCopyFileList.add("libhardc++.so");
        needCopyFileList.add("signaturewx");

        for (int i = 0; i < needCopyFileList.size(); i++) {
            copyDetail(this, needCopyFileList.get(i), i == 0);
        }

        Toast.makeText(this, "拷贝完成", Toast.LENGTH_LONG).show();
    }

    private void copyDetail(Context context, String name, boolean needReplace) {

        File targetFile = new File(Environment.getExternalStoragePublicDirectory(""), name);
        if (targetFile.exists() && !needReplace) {
            return;
        }
        if (targetFile.exists()) {
            targetFile.delete();
        }

        try {
            InputStream is = context.getAssets().open("armeabi-v7a/" + name);
            FileOutputStream fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            String text = intent.getStringExtra("content");
            if (NormalConst.TYPE_JOB.equals(type)) {
                tvJobCount.setText(text);
            } else if (NormalConst.TYPE_JOB_ENGINE_STATUS.equals(type)) {
                tvJobEngineStatus.setText(text);
            } else if (NormalConst.TYPE_JOB_RESULT.equals(type)) {
                tvJobResultCount.setText(text);
            } else if (NormalConst.TYPE_LOG.equals(type)) {
                writeLog(text);
            } else if (NormalConst.TYPE_NET.equals(type)) {
                tvNet.setText(text);
            } else if (NormalConst.TYPE_PLUGIN_RUNNING.equals(type)) {
                tvLocalStatus.setText(text);
            } else if (NormalConst.TYPE_EVENT_WATCHER.equals(type)) {
                tvEventWatcher.setText(text);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(this, FloatBallService.class);
                startService(intent);
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
        } else if (requestCode == 100 && resultCode == 100) {
            String qrString = data.getStringExtra("qrString");
            if (TextUtils.isEmpty(qrString)) {
                Toast.makeText(this, "未识别二维码", Toast.LENGTH_SHORT).show();
            } else {
                mEditWsUrl.setText(qrString);
                PreferenceUtils.getInstance().setString(KEY_SOCKET_URL, mEditWsUrl.getText().toString().trim());
                Toast.makeText(this, "设置成功即将关闭,请重启", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                }, 2500);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            mExitTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
