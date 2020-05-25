package com.mango.puppetmodel;

import java.util.Map;

/**
 * Event
 * 事件模型
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class Event {

    // 事件名称
    public String event_name;

    // 监听哪个应用，系统应用不需此参数
    public String package_name;

    // 1:注册 0:注销
    public Map<Object, Object> event_data;

    // 事件上报结果(仅本地控制器使用) 0:新事件未上报 1:上报已成功
    public int event_status;
}
