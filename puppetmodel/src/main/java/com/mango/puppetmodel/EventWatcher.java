package com.mango.puppetmodel;

/**
 * EventWatcher
 * 注册/注销事件监听的模型
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class EventWatcher {

    // 事件名称
    public String event_name;

    // 监听哪个应用，系统应用不需此参数
    public String package_name;

    // 1:注册 0:注销
    public int watcher_status;

}
