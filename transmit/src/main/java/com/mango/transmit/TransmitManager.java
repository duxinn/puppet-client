package com.mango.transmit;

import android.content.Context;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;
import com.mango.transmit.i.IEventTransform;
import com.mango.transmit.i.ITransmitReceiver;
import com.mango.transmit.i.ITransmitSender;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * TransmitManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class TransmitManager implements ITransmitSender, IEventTransform {
    private static final TransmitManager ourInstance = new TransmitManager();

    public static TransmitManager getInstance() {
        return ourInstance;
    }

    private TransmitManager() {
    }

    /************   public   ************/

    /**
     * 用于registerReceiver和Filter用以接收数据
     * @param context context
     * @param filters 木马插件所在应用的包名
     */
    public void setRegister(Context context, ArrayList<String> filters) {

    }

    public void setTransmitReceiver(ITransmitReceiver receiver) {

    }

    /************   IEventTransform   ************/
    @Override
    public void transformEvent(String packageName, Event event, IIEventTransformCallback callback) {

    }

    /************   ITransmitSender   ************/
    @Override
    public void sendJob(String targetPackageName, String messageId, ITransmitCallback callback, Job job) {

    }

    @Override
    public void sendJob(String targetPackageName, String messageId, ITransmitCallback callback, Job job, long timeoutMillis) {

    }

    @Override
    public void sendEventWatcher(String targetPackageName, String messageId, ITransmitCallback callback, EventWatcher eventWatcher) {

    }

    @Override
    public void sendEventWatcher(String targetPackageName, String messageId, ITransmitCallback callback, EventWatcher eventWatcher, long timeoutMillis) {

    }

    @Override
    public void sendEvent(String targetPackageName, String messageId, ITransmitCallback callback, Event event) {

    }

    @Override
    public void sendEvent(String targetPackageName, String messageId, ITransmitCallback callback, Event event, long timeoutMillis) {

    }

    @Override
    public void sendData(String targetPackageName, String messageId, ITransmitCallback callback, byte[] data) {

    }

    @Override
    public void sendData(String targetPackageName, String messageId, ITransmitCallback callback, byte[] data, long timeoutMillis) {

    }

    @Override
    public void sendMessage(String targetPackageName, String messageId, ITransmitCallback callback, JSONObject data) {

    }

    @Override
    public void sendMessage(String targetPackageName, String messageId, ITransmitCallback callback, JSONObject data, long timeoutMillis) {

    }
}
