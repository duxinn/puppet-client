package com.mango.puppet.dispatch.event;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mango.puppet.dispatch.event.i.IEvent;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.network.NetworkManager;
import com.mango.puppet.network.i.INetwork;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginControl;
import com.mango.puppet.status.StatusManager;
import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    EventWatcher eventWatcherModel = new EventWatcher();
    List<EventWatcher> eventWatcherList = new ArrayList<>();
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

        String url = null;
        for (int i = 0; i < eventWatcherList.size(); i++) {
            if (event.package_name.equals(eventWatcherList.get(i).package_name) && event.event_name.equals(eventWatcherList.get(i).event_name)) {
                url = eventWatcherList.get(i).callback;
            }
        }
        LogManager.getInstance().recordLog("上报新事件"+event.event_name + url);
        if (event != null && url != null) {
            NetworkManager.getInstance().reportEvent(url, event, new INetwork.IEventRequestResult() {
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
        } else {
            event.event_status = 0;
            PluginManager.getInstance().distributeEvent(event, iPluginControlResult);
        }
    }

    @Override
    public void setEventWatcher(String packageName, String eventName, boolean isValid, String url) {
        LogManager.getInstance().recordDebugLog(isValid?"注册新事件":"注销新事件"+packageName+" "+eventName+" ");
        int watchStatus;
        Boolean isNeed = true;
        if (packageName != null && eventName != null) {
            if (isValid) {
                watchStatus = 1;
            } else {
                watchStatus = 0;
            }
            StatusManager.getInstance().setEventWatcher(packageName, eventName, isValid);
            EventWatcher model = new EventWatcher();
            model.package_name = packageName;
            model.event_name = eventName;
            model.watcher_status = watchStatus;
            model.callback = url;
            PluginManager.getInstance().distributeEventWatcher(model, iPluginControlResult);
            SharedPreferences mContextSharedPreferences = mContext.getSharedPreferences("saveEventJson", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mContextSharedPreferences.edit();
            for (int i = 0; i < eventWatcherList.size(); i++) {
                if (packageName.equals(eventWatcherList.get(i).package_name) && eventName.equals(eventWatcherList.get(i).event_name)) {
                    if (!isValid) {
                        eventWatcherList.remove(i);
                        i = i - 1;
                    } else {
                        eventWatcherList.get(i).callback = url;
                        eventWatcherList.get(i).watcher_status = watchStatus;
                    }
                    isNeed = false;
                }
            }
            if (isNeed) {
                eventWatcherList.add(model);
            }
            String eventJson = new Gson().toJson(eventWatcherList);
            editor.putString("eventJson", eventJson);
            editor.commit();
        }
    }

    @Override
    public boolean startEventSystem(Context context) {
        LogManager.getInstance().recordDebugLog("启动事件系统");
        mContext = context;
        Boolean isValid;
        SharedPreferences read = context.getSharedPreferences("saveEventJson", Context.MODE_PRIVATE);
        String readEvent = read.getString("eventJson", "");
        if (!readEvent.equals("")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<EventWatcher>>() {
            }.getType();
            eventWatcherList = gson.fromJson(readEvent, type);
        }
        for (int i = 0; i < eventWatcherList.size(); i++) {
            if (eventWatcherList.get(i).watcher_status == 1) {
                isValid = true;
            } else {
                isValid = false;
            }
            StatusManager.getInstance().setEventWatcher(eventWatcherList.get(i).package_name, eventWatcherList.get(i).event_name, isValid);
            PluginManager.getInstance().distributeEventWatcher(eventWatcherList.get(i), iPluginControlResult);
        }
        return true;
    }
}
