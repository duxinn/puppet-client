package com.mango.puppetsystem;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.mango.puppet.tool.PreferenceUtils;
import com.mango.puppetsystem.floatball.FloatBallService;

import java.io.DataOutputStream;
import java.util.ArrayList;

import static com.mango.puppet.network.wsmanager.WsManager.KEY_SOCKET_URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout retryLL;
    private ArrayList<String> logList = new ArrayList<>();
    private TextView tvLog, tvNet, tvJobCount, tvJobResultCount, tvJobEngineStatus, tvLocalStatus, tvEventWatcher;
    private EditText mEditWsUrl;
    private Button mBtnSetWsUrl;
    private MyReceiver myReceiver;
    private long mExitTime = 0;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
        boolean isRoot = hasRoot();
        if (!isRoot) {
            writeLog("请先开启ROOT权限");
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        retryLL = findViewById(R.id.retryTv);
        mEditWsUrl = findViewById(R.id.edit_ws_url);
        mBtnSetWsUrl = findViewById(R.id.btn_set_ws_url);
        mBtnSetWsUrl.setOnClickListener(this);

        tvLog = findViewById(R.id.tvlog);
        tvNet = findViewById(R.id.tvNetStatus);
        tvJobCount = findViewById(R.id.tvJob);
        tvJobResultCount = findViewById(R.id.tvJobResult);
        tvJobEngineStatus = findViewById(R.id.tvJobEngineStatus);
        tvLocalStatus = findViewById(R.id.tvLocalStatus);
        tvEventWatcher = findViewById(R.id.tvEventWatcher);
    }

    private void initData() {
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver, new IntentFilter(NormalConst.ACTION));
    }

    private void initEvent() {
        retryLL.setOnClickListener(this);
        int permission = ActivityCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
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


    // 判断及申请root权限
    public static boolean hasRoot() {
        boolean flag = true;
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
            flag = false;
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
        return flag;
    }

    private void startFloatBallService() {
        if (FloatBallService.isStarted) {
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
        } else {
            Intent intent = new Intent(this, FloatBallService.class);
            startService(intent);

            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.retryTv) {
            startFloatBallService();
        } else if (v.getId() == R.id.btn_set_ws_url) {
            if (!TextUtils.isEmpty(mEditWsUrl.getText().toString().trim())) {
                PreferenceUtils.getInstance().setString(KEY_SOCKET_URL, mEditWsUrl.getText().toString().trim());
                System.exit(1);
            }
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
