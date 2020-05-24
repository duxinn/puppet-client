package com.mango.hookdemoplugin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.mango.hookdemo.MainActivity;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.mango.transmit.TransmitManager;
import com.mango.transmit.i.ITransmitReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Entrance
 *
 * @author: hehongzhen
 * @date: 2020/05/20
 */
public class Entrance implements ITransmitReceiver {

    public static Entrance instance = new Entrance();

    private static int count = 0;

    static void start() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.mango.puppetsystem");
        TransmitManager.getInstance().setRegister(MainActivity.mainActivity, list);
        TransmitManager.getInstance().setTransmitReceiver(instance);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initLooper();
            }
        }).start();
    }

    private static void initLooper() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (count != MainActivity.mainActivity.arrayList.size()) {
                count = MainActivity.mainActivity.arrayList.size();
                String s = MainActivity.mainActivity.arrayList.get(MainActivity.mainActivity.arrayList.size() - 1);

                Event event = new Event();
                event.event_name = "array_add";
                event.event_status = 0;
                event.package_name = MainActivity.mainActivity.getPackageName();
                event.event_data = new HashMap();
                event.event_data.put("content", s);
                TransmitManager.getInstance().sendEvent(
                        null,
                        event);
            }
        }
    }

    @Override
    public void onReceiveJob(String packageName, Job job) {
        if ("com.mango.hookdemo".equals(packageName)) {
            if (job.job_name.equals("abc")) {
                String content = null;
                try {
                    content = job.job_data.getString("text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MainActivity.mainActivity.mTv.setText(content);
                job.job_status = 2;
                TransmitManager.getInstance().sendJob(
                        null,
                        job);
            } else {
                job.job_status = 4;
                job.error_code = -3;
                job.error_message = "未知任务";
                TransmitManager.getInstance().sendJob(
                        null,
                        job);
            }
        } else {
            job.job_status = 4;
            job.error_code = -2;
            job.error_message = "下发错误";
            TransmitManager.getInstance().sendJob(
                    null,
                    job);
        }
    }

    @Override
    public void onReceiveEvent(String packageName, Event event) {
        // 木马插件方 do nothing
    }

    @Override
    public void onReceiveEventWatcher(String packageName, EventWatcher eventWatcher) {
        if (eventWatcher.event_name.equals("array_add")) {
            if (eventWatcher.watcher_status == 1) {
                // start looper
            } else {
                // stop looper
            }
        }
    }

    @Override
    public void onReceiveEventData(String packageName, String dataString) {

    }

    @Override
    public void onReceiveEventData(String packageName, JSONObject jsonObject) {

    }
}
