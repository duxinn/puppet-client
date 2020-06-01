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

import com.mango.puppet.dispatch.event.EventManager;
import com.mango.puppet.dispatch.job.JobManager;
import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.dispatch.system.SystemManager;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.log.i.ILog;
import com.mango.puppet.network.CallBackListener;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.status.i.IStatusListener;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.Job;
import com.mango.puppetsystem.AppApplication;
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
    private int hasRegisterEventWatcher = 0;
    private TextView mTvEventWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        LogManager.getInstance().setNewLogListener(this);
        StatusManager.getInstance().setStatusListener(this);
        init();
        // todo 测试代码，临时解决方案
        LogManager.init(this);
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
            LinearLayout llSendJob = displayView.findViewById(R.id.ll_send_job);
            LinearLayout llSendDelayJob = displayView.findViewById(R.id.ll_send_delay_job);
            LinearLayout llSendContinueJob = displayView.findViewById(R.id.ll_send_continuous_job);
            LinearLayout llSendErrorJob = displayView.findViewById(R.id.ll_error_job);
            LinearLayout llSendCancelJob = displayView.findViewById(R.id.ll_cancel_job);
            LinearLayout llSingleSendErrorJob = displayView.findViewById(R.id.ll_send_error_job);
            LinearLayout llSendRetryJob = displayView.findViewById(R.id.ll_retry_job);
            LinearLayout llSendEventWatcher = displayView.findViewById(R.id.ll_event_watcher);
            mTvEventWatcher= displayView.findViewById(R.id.tv_event_watcher);
            LinearLayout llSendEvent = displayView.findViewById(R.id.ll_send_event);
            LinearLayout llClearDB = displayView.findViewById(R.id.ll_clear_db);
            tvLog = displayView.findViewById(R.id.tvlog);
            tvNet = displayView.findViewById(R.id.tvNetStatus);
            tvJobCount = displayView.findViewById(R.id.tvJob);
            tvJobResultCount = displayView.findViewById(R.id.tvJobResult);
            tvJobEngineStatus = displayView.findViewById(R.id.tvJobEngineStatus);
            tvLocalStatus = displayView.findViewById(R.id.tvLocalStatus);
            tvEventWatcher = displayView.findViewById(R.id.tvEventWatcher);
            StatusManager.getInstance().setNetworkStatus(StatusManager.getInstance().getNetworkStatus());//重设网络状态
            llRetry.setOnClickListener(this);
            llSendJob.setOnClickListener(this);
            llSendDelayJob.setOnClickListener(this);
            llSendContinueJob.setOnClickListener(this);
            llSendErrorJob.setOnClickListener(this);
            llSendCancelJob.setOnClickListener(this);
            llSingleSendErrorJob.setOnClickListener(this);
            llSendRetryJob.setOnClickListener(this);

            llSendEventWatcher.setOnClickListener(this);
            llSendEvent.setOnClickListener(this);
            llClearDB.setOnClickListener(this);
            windowManager.addView(displayView, layoutParams);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llRetry:
                SystemManager.getInstance().startSystem(AppApplication.instance);
                break;

            case R.id.ll_send_job:
                CallBackListener.getInstance().sendSuccessJob();
                break;

            case R.id.ll_send_delay_job:
                CallBackListener.getInstance().sendDelayJob();
                break;

            case R.id.ll_send_continuous_job:
                CallBackListener.getInstance().sendContinuousJob();
                break;

            case R.id.ll_error_job:
                CallBackListener.getInstance().sendFailedJob();
                break;

            case R.id.ll_send_error_job:
                CallBackListener.getInstance().sendFailedAndReportJob();
                break;

            case R.id.ll_cancel_job:
                CallBackListener.getInstance().sendCancelJob();
                break;

            case R.id.ll_retry_job:
                CallBackListener.getInstance().sendRetryJob();
                break;

            case R.id.ll_event_watcher:
                if (hasRegisterEventWatcher == 0) {
                    mTvEventWatcher.setText("注销事件");
                    hasRegisterEventWatcher = 1;
                    CallBackListener.getInstance().sendEventWatcher();
                } else {
                    mTvEventWatcher.setText("注册事件");
                    hasRegisterEventWatcher = 0;
                }
                break;

            case R.id.ll_send_event:
                Event event = new Event();
                event.event_name = "sendMessage";
                EventManager.getInstance().uploadNewEvent(event);
                break;

            case R.id.ll_clear_db:
                DBManager.clearDB();
                break;
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
        tvNet.setText(isNetOk ? "服务器运行中" : "服务器已停止");
        sendBroadCastToActivity(tvNet.getText().toString(), NormalConst.TYPE_NET);
    }

    @Override
    public void onJobEngineStatusChanged(int status) {
        String text;
        if (status == 1) {
            text = "执行中";
        } else if (status == 0) {
            text = "等待新任务";
        } else if (status == -1) {
            text = "有失败任务";
        } else if (status == -2) {
            text = "等待任务结果上报完成";
        } else {
            text = "未知状态";
        }
        tvJobEngineStatus.setText(text);
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
