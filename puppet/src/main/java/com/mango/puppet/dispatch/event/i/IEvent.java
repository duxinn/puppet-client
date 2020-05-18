package com.mango.puppet.dispatch.event.i;

import com.mango.puppetmodel.Event;

/**
 * IEvent
 * 事件调度
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IEvent {

    /**
     * 上报新事件
     * @param event 新事件
     */
    void uploadNewEvent(Event event);

    /**
     * 注册/注销新事件
     * @param packageName 目标应用包名
     * @param eventName 事件名
     * @param isValid true注册 false注销
     */
    void setEventWatcher(String packageName,
                         String eventName,
                         boolean isValid);
}
