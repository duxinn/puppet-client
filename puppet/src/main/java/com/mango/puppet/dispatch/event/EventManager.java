package com.mango.puppet.dispatch.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.mango.puppet.dispatch.event.i.IEvent;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * EventManager
 * 事件调度
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class EventManager implements IEvent {
    private static final EventManager ourInstance = new EventManager();
    Context mContext;
    Map<String, String> eventMap = new HashMap<>();
    Boolean isNeed;
    IPluginControl.IPluginControlResult iPluginControlResult = new IPluginControl.IPluginControlResult() {
        @Override
        public void onFinished(boolean isSucceed, String failReason) {
            Log.i("OnEventManager", "onFinished: -----" + isSucceed + failReason);
        }
    };

    public static EventManager getInstance() {
        return ourInstance;
    }

    private EventManager() {
    }

    /************   IEvent   ************/
    @Override
    public void uploadNewEvent(Event event) {
        if (event != null){
            NetworkManager.getInstance().reportEvent(event, new INetwork.IEventRequestResult() {
                @Override
                public void onSuccess(Event event) {
                    event.event_status = 1;
                    PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
                }

                @Override
                public void onError(Event event, int errorCode, String errorMessage) {
                    event.event_status = 1;
                    PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
                }

                @Override
                public void onNetworkError(Event event) {
                    event.event_status = 0;
                    PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
                }
            });
        }
    }

    @Override
    public void setEventWatcher(String packageName, String eventName, boolean isValid) {
        int watchStatus;
        if (packageName != null && eventName != null) {
            if (isValid) {
                watchStatus = 1;
            } else {
                watchStatus = 0;
            }
            StatusManager.getInstance().setEventWatcher(packageName, eventName, isValid);
            EventWatcher model = new EventWatcher();
            model.setPackageName(packageName);
            model.setEventName(eventName);
            model.setWatcherStatus(watchStatus);
            PluginManager.getInstance().distributeEventWatcher(model, iPluginControlResult);
            Gson gson = new Gson();
            String eventJson = gson.toJson(model);
            SharedPreferences mContextSharedPreferences=  mContext.getSharedPreferences("writeEventJson", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mContextSharedPreferences.edit();
            if (eventMap.get(eventName) != null && !isValid) {
                editor.remove(eventName);
            } else {
                editor.putString(eventName, eventJson);
            }
            editor.commit();
            StatusManager.getInstance().setEventWatcher(packageName, eventName, isValid);
        }
    }

    @Override
    public boolean startEventSystem(Context context) {
        mContext = context;
        SharedPreferences read = context.getSharedPreferences("readEventJson", Context.MODE_PRIVATE);
        eventMap = (Map<String, String>) read.getAll();
        return true;
    }
}
