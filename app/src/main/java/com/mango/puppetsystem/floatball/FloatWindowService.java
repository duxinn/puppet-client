package com.mango.puppetsystem.floatball;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mango.puppet.dispatch.system.SystemManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.log.i.ILog;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.status.i.IStatusListener;
import com.mango.puppetsystem.NormalConst;
import com.mango.puppetsystem.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FloatWindowService extends Service implements View.OnClickListener, ILog.ILogListener, IStatusListener {

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View displayView;
    private ArrayList<String> logList = new ArrayList<>();
    private TextView tvLog, tvNet, tvJobCount, tvJobResultCount, tvJobEngineStatus, tvLocalStatus, tvEventWatcher;

    private int injectStatus = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LogManager.getInstance().setNewLogListener(this);
        StatusManager.getInstance().setStatusListener(this);
        init();
        showFloatingWindow();
    }

    public void init() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        displayView.findViewById(R.id.btnFloating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayView.setVisibility(View.GONE);
                Intent intent = new Intent(FloatWindowService.this, FloatBallService.class);
                intent.putExtra("injectStatus", injectStatus);
                startService(intent);
                FloatBallService.flag = true;
            }
        });
        displayView.setVisibility(View.VISIBLE);
        return super.onStartCommand(intent, flags, startId);
    }

    public void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.view_floatwindow, null);
            LinearLayout llRetry = displayView.findViewById(R.id.llRetry);
            tvLog = displayView.findViewById(R.id.tvlog);
            tvNet = displayView.findViewById(R.id.tvNetStatus);
            tvJobCount = displayView.findViewById(R.id.tvJob);
            tvJobResultCount = displayView.findViewById(R.id.tvJobResult);
            tvJobEngineStatus = displayView.findViewById(R.id.tvJobEngineStatus);
            tvLocalStatus = displayView.findViewById(R.id.tvLocalStatus);
            tvEventWatcher = displayView.findViewById(R.id.tvEventWatcher);
            StatusManager.getInstance().setNetworkStatus(StatusManager.getInstance().getNetworkStatus());//重设网络状态
            llRetry.setOnClickListener(this);
            windowManager.addView(displayView, layoutParams);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.llRetry) {
            SystemManager.getInstance().startSystem();
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

    @Override
    public void onDestroy() {
        if (windowManager != null && displayView != null) {
            windowManager.removeView(displayView);
        }
        super.onDestroy();
    }


    @Override
    public void onNewLog(String log) {
        writeLog(log);
        sendBroadCastToActivity(log, NormalConst.TYPE_LOG);
    }

    @Override
    public void onNetworkStatusChanged(boolean isNetOk) {
        tvNet.setText(isNetOk ? "网络状态良好" : "请检查网络状态");
        sendBroadCastToActivity(tvNet.getText().toString(), NormalConst.TYPE_NET);
    }

    @Override
    public void onJobEngineStatusChanged(int status) {
        tvJobEngineStatus.setText(status == 0 ? "任务引擎停止" : "任务引擎正常");
        sendBroadCastToActivity(tvJobEngineStatus.getText().toString(), NormalConst.TYPE_JOB_ENGINE_STATUS);
    }

    @Override
    public void onEventWatcherChanged() {
        StringBuilder sb = new StringBuilder();
        sb.append("监听事件: ");
        Map<String, List<String>> map = StatusManager.getInstance().getAllEventWatcher();
        if (map != null) {
            for (String key : map.keySet()) {
                List<String> strings = map.get(key);
                if (strings != null && strings.size() > 0) {
                    sb.append("\n").append(key).append("(");
                    for (String s : map.get(key)) {
                        sb.append(s).append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1).append(")");
                }
            }
        }
        tvEventWatcher.setText(sb.toString());
        sendBroadCastToActivity(tvEventWatcher.getText().toString(), NormalConst.TYPE_EVENT_WATCHER);
    }

    @Override
    public void onPluginRunningChanged() {
        StringBuilder sb = new StringBuilder();
        sb.append("所有插件状态:  ");
        List<String> list = StatusManager.getInstance().getAllRunningPlugin();
        if (list == null || list.size() == 0)
            return;
        for (String s : list) {
            sb.append("\n").append(s).append(":  ").append(StatusManager.getInstance().isPluginRunning(s) ? "正在运行" : "未运行");
            if (StatusManager.getInstance().isPluginRunning(s)) {
                Intent intent = new Intent(this, FloatBallService.class);
                injectStatus = 1;
                intent.putExtra("injectStatus", injectStatus);
                startService(intent);
            }
        }
        tvLocalStatus.setText(sb);
        sendBroadCastToActivity(sb.toString(), NormalConst.TYPE_PLUGIN_RUNNING);
    }

    @Override
    public void onJobCountChanged(int count) {
        tvJobCount.setText(count + "");
        sendBroadCastToActivity(count + "", NormalConst.TYPE_JOB);
    }

    @Override
    public void onJobResultCountChanged(int count) {
        tvJobResultCount.setText(count + "");
        sendBroadCastToActivity(count + "", NormalConst.TYPE_JOB_RESULT);
    }


    private void sendBroadCastToActivity(String content, String type) {
        Intent intent = new Intent();
        intent.putExtra("content", content);
        intent.putExtra("type", type);
        intent.setAction(NormalConst.ACTION);
        sendBroadcast(intent);
    }
}
