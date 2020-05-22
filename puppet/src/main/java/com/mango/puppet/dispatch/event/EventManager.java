package com.mango.puppet.dispatch.event;

import android.content.Context;
import android.os.Environment;

import com.mango.puppet.dispatch.event.i.IEvent;
import com.mango.puppetmodel.Event;

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
    public static EventManager getInstance() {
        return ourInstance;
    }

    private EventManager() {
    }

    /************   IEvent   ************/
    @Override
    public void uploadNewEvent(Event event) {

    }

    @Override
    public void setEventWatcher(String packageName, String eventName, boolean isValid) {

    }

    @Override
    public boolean startEventSystem(Context context) {
        mContext = context;
        return false;
    }
}
