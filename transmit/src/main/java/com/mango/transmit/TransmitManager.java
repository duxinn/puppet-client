package com.mango.transmit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.mango.transmit.i.IEventTransform;
import com.mango.transmit.i.ITransmitReceiver;
import com.mango.transmit.i.ITransmitSender;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TransmitManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class TransmitManager implements ITransmitSender, IEventTransform {

    public static final String MANAGER_PACKAGE_NAME = "com.mango.puppetsystem";
    public static final String TYPE_KEY = "TYPE_KEY";
    public static final String DATA_KEY = "DATA_KEY";
    public static final String JOB_KEY = "JOB_KEY";
    public static final String EVENT_KEY = "EVENT_KEY";
    public static final String EVENT_WATCHER_KEY = "EVENT_WATCHER_KEY";
    public static final String FIRST_START_KEY = "FIRST_START_KEY";
    public static final String HEART_KEY = "HEART_KEY";
    // 上传文件
    public static final String UPLOAD_KEY = "UPLOAD_KEY";
    public static final String LOCAL_URL_KEY = "LOCAL_URL_KEY";
    public static final String FILE_NAME = "FILE_NAME";
    public static final String REMOTE_URL_KEY = "REMOTE_URL_KEY";


    private static final String CONTENT_KEY = "CONTENT_KEY";
    private static final String PACKAGE_KEY = "PACKAGE_KEY";
    private static final TransmitManager ourInstance = new TransmitManager();
    private static final String RECEIVE_FORE_STRING = "RECEIVE_FORE_STRING_";
    private static final String HookTypeEndString = "^HookTypeEndString^";
    private static final String HookTypeEndEncodeString = "\\^HookTypeEndString\\^";

    public static TransmitManager getInstance() {
        return ourInstance;
    }

    private WeakReference<Context> mContextReference = null;
    private BroadcastReceiver mReceiver = null;
    private ITransmitReceiver mListener = null;
    private Map<String, String> mTmpDataMap = null;

    private TransmitManager() {
    }

    public void destroy() {
        if (getContext() != null && mReceiver != null) {
            getContext().unregisterReceiver(mReceiver);
            mContextReference = null;
            mReceiver = null;
        }
    }

    /*   public   */

    /**
     * 用于registerReceiver和Filter用以接收数据
     * @param context context
     * @param filters 木马插件所在应用的包名
     */
    public void setRegister(Context context, ArrayList<String> filters) {
        for (int i = 0; i < filters.size(); i++) {
            Log.d("TransmitManager", "setRegister:" + filters.get(i));
        }
        if (context == null || filters == null) {
            return;
        }
        if (getContext() != null && mReceiver != null) {
            getContext().unregisterReceiver(mReceiver);
            mContextReference = null;
            mReceiver = null;
        }
        final ArrayList<String> appendedFilters = new ArrayList<>();
        mContextReference = new WeakReference<>(context);
        IntentFilter intentFilter = new IntentFilter();
        for (int i = 0; i < filters.size(); i++) {
            String s = RECEIVE_FORE_STRING + filters.get(i);
            intentFilter.addAction(s);
            appendedFilters.add(s);
        }
        mTmpDataMap = new HashMap<>();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (appendedFilters.contains(intent.getAction())) {
                    String content = intent.getStringExtra(CONTENT_KEY);
                    String packageName = intent.getStringExtra(PACKAGE_KEY);
                    dealReceiveContent(packageName, content);
                }
            }
        };
        getContext().registerReceiver(mReceiver, intentFilter);
    }

    public void setTransmitReceiver(ITransmitReceiver receiver) {
        mListener = receiver;
    }

    /************   IEventTransform   ************/
    @Override
    public void transformEvent(String packageName, Event event, IIEventTransformCallback callback) {

    }

    /************   ITransmitSender   ************/
    @Override
    public void sendJob(String targetPackageName, Job job) {
        if (job != null) {
            String data = job.toString();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(TYPE_KEY, JOB_KEY);
                jsonObject.put(DATA_KEY, data);
                sendData(targetPackageName, jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendEventWatcher(String targetPackageName, EventWatcher eventWatcher) {
        if (eventWatcher != null) {
            String data = new Gson().toJson(eventWatcher);
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(TYPE_KEY, EVENT_WATCHER_KEY);
                jsonObject.put(DATA_KEY, data);
                sendData(targetPackageName, jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendEvent(String targetPackageName, Event event) {
        if (event != null) {
            String data = event.toString();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(TYPE_KEY, EVENT_KEY);
                jsonObject.put(DATA_KEY, data);
                sendData(targetPackageName, jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendData(String targetPackageName, String data) {
        sendString(targetPackageName, data);
    }

    @Override
    public void sendMessage(String targetPackageName, JSONObject data) {
        if (data != null) {
            sendData(targetPackageName, data.toString());
        }
    }


    /*   private   */

    /**
     * 获取context
     * @return context
     */
    private Context getContext() {
        if (mContextReference != null) {
            return mContextReference.get();
        }
        return null;
    }

    /**
     * 确保在主线程处理接收到的内容
     *
     * @param packageName 发送方包名
     * @param content 接收到的内容
     */
    private void dealReceiveContent(final String packageName, final String content) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    splitData(packageName, content);
                }
            });
        } else {
            splitData(packageName, content);
        }
    }

    /**
     * 拆解数据
     *
     * @param packageName 发送方包名
     * @param content 接收到的内容
     */
    private void splitData(String packageName, String content) {
        if (mListener == null) return;
        if (content == null) return;
        if (TextUtils.isEmpty(packageName)) return;

        String tmpString = mTmpDataMap.get(packageName);
        if (tmpString == null) {
            tmpString = "";
        }
        if (!content.endsWith(HookTypeEndString)) {
            tmpString = tmpString + content;
            mTmpDataMap.put(packageName, tmpString);
        } else {
            String contentData = tmpString + content;
            mTmpDataMap.put(packageName, "");
            String[] dataList = contentData.split(HookTypeEndEncodeString);
            for (String s : dataList) {
                dealData(packageName, s);
            }
        }
    }

    /**
     * 处理数据
     *
     * @param packageName 发送方包名
     * @param content 接收到的内容
     */
    private void dealData(String packageName, String content) {
        if (mListener == null) return;
        if (!content.contains(HEART_KEY)) {
            Log.d("TransmitManager", "onReceiveDataString:" + packageName + " " + content);
        }
        mListener.onReceiveDataString(packageName, content);
        try {
            JSONObject jsonObject = new JSONObject(content);
            mListener.onReceiveData(packageName, jsonObject);

            String type = jsonObject.getString(TYPE_KEY);
            if (JOB_KEY.equals(type)) {
                Job job = Job.fromString(jsonObject.getString(DATA_KEY));
                mListener.onReceiveJob(packageName, job);
            } else if (EVENT_KEY.equals(type)) {
                Event event = Event.fromString(jsonObject.getString(DATA_KEY));
                mListener.onReceiveEvent(packageName, event);
            } else if (EVENT_WATCHER_KEY.equals(type)) {
                EventWatcher eventWatcher = new Gson().fromJson(jsonObject.getString(DATA_KEY), EventWatcher.class);
                mListener.onReceiveEventWatcher(packageName, eventWatcher);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送内容
     * @param targetPackageName 目标进程包名
     * @param string 内容
     */
    private void sendString(String targetPackageName, String string) {
        if (string != null && getContext() != null) {
            if (!string.contains(HEART_KEY)) {
                Log.d("TransmitManager", "sendString:" + targetPackageName + " " + string);
            }
            String sendString = string + HookTypeEndString;
            double divideLength = 10000.0;
            int divideInt = 10000;
            long count = (long)Math.ceil(sendString.length() / divideLength);
            for (int i = 0; i < count; i++) {
                Intent intent = new Intent(RECEIVE_FORE_STRING + targetPackageName);
                String content = sendString.substring(i * divideInt, Math.min(sendString.length(), (i + 1) * divideInt));
                intent.putExtra(CONTENT_KEY, content);
                intent.putExtra(PACKAGE_KEY, getContext().getPackageName());
                getContext().sendBroadcast(intent);
            }
        }
    }

}
