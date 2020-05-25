package com.mango.transmit.i;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;

import org.json.JSONObject;

/**
 * ITransmitReceiver
 * 处理收到的数据 其他模块使用transmit模块进行进程间通信时实现
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ITransmitReceiver {

    /**
     * 收到任务
     *
     * @param packageName 发送消息的应用的包名
     * @param job 任务
     */
    void onReceiveJob(String packageName,
                      Job job);

    /**
     * 收到事件
     *
     * @param packageName 发送消息的应用的包名
     * @param event 事件
     */
    void onReceiveEvent(String packageName,
                        Event event);

    /**
     * 收到注册/注销事件
     *
     * @param packageName 发送消息的应用的包名
     * @param eventWatcher 注册/注销事件
     */
    void onReceiveEventWatcher(String packageName,
                               EventWatcher eventWatcher);

    /**
     * 收到String内容
     *
     * @param packageName 发送消息的应用的包名
     * @param dataString 收到的内容
     */
    void onReceiveDataString(String packageName,
                             String dataString);

    /**
     * 收到JSONObject内容
     *
     * @param packageName 发送消息的应用的包名
     * @param jsonObject 收到的内容
     */
    void onReceiveData(String packageName,
                       JSONObject jsonObject);
}
