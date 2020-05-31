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

    public static final String EVENT_MESSAGE = "chat_message";
    public static final String EVENT_NEW_FRIEND = "new_friend_request";

    // 事件名称
    public String event_name;

    // 监听哪个应用，系统应用不需此参数
    public String package_name;

    // 1:注册 0:注销
    public int watcher_status;

    // 注册或注销失败时的错误码 任务成功时为0
    public int error_code;

    // 注册或注销失败时的原因 任务成功时为null
    public String error_message;

    // 注册或注销事件后回调url
    public String callback;
}
