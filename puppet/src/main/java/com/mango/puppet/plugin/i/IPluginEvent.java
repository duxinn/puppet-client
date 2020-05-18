package com.mango.puppet.plugin.i;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;

/**
 * IPluginEvent
 * event相关接口
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IPluginEvent {

    /**
     * 注册/注销事件的监听
     *
     * @param eventWatcher 注册/注销事件
     * @param result 是否成功
     */
    void distributeEventWatcher(EventWatcher eventWatcher,
                                IPluginControl.IPluginControlResult result);

    /**
     * 向插件传递已经上传完毕的事件 用于记录事件进度
     *
     * @param event 已经上传完毕的事件
     * @param result 是否传递成功
     */
    void distributeEvent(Event event,
                         IPluginControl.IPluginControlResult result);

}
