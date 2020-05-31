package com.wzg.trojandemo;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;

import com.mango.puppetmodel.Job;
import com.mango.transmit.TransmitManager;
import com.mango.transmit.i.ITransmitReceiver;

import org.json.JSONObject;

/**
 * DataReceiver
 *
 * @author: hehongzhen
 * @date: 2020/05/26
 */
public class DataReceiver implements ITransmitReceiver {

    private static final DataReceiver ourInstance = new DataReceiver();

    public static DataReceiver getInstance() {
        return ourInstance;
    }

    private DataReceiver() {
    }

    @Override
    public void onReceiveJob(String packageName, com.mango.puppetmodel.Job job) {
        if (MyApplication.instance.getPackageName().equals(job.package_name)) {
            // 假设任务做完，上报任务结果
            job.result_data = "任务已完成";
            job.job_status = 3; // 3成功 5失败
            TransmitManager.getInstance().sendJob(TransmitManager.MANAGER_PACKAGE_NAME, job);
        }
    }

    @Override
    public void onReceiveEvent(String packageName, com.mango.puppetmodel.Event event) {
        if (MyApplication.instance.getPackageName().equals(event.package_name)) {
            // 假设事件做完，上报事件结果
            event.event_status = 0;
            TransmitManager.getInstance().sendEvent(TransmitManager.MANAGER_PACKAGE_NAME, event);
        }
    }

    @Override
    public void onReceiveEventWatcher(String packageName, EventWatcher eventWatcher) {
        if (MyApplication.instance.getPackageName().equals(eventWatcher.package_name)) {
            // 假设注册完成，上报注册结果
            eventWatcher.error_code = 0; // 注册/注销成功
            TransmitManager.getInstance().sendEventWatcher(TransmitManager.MANAGER_PACKAGE_NAME, eventWatcher);
        }
    }

    @Override
    public void onReceiveDataString(String packageName, String dataString) {

    }

    @Override
    public void onReceiveData(String packageName, JSONObject jsonObject) {

    }
}
