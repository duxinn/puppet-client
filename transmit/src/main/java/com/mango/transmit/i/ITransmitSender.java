package com.mango.transmit.i;

import com.mango.puppetmodel.Event;
import com.mango.puppetmodel.EventWatcher;
import com.mango.puppetmodel.Job;

import org.json.JSONObject;

/**
 * ITransmitSender
 * 进程间传输接口
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ITransmitSender {

    /**
     * 进程间通信发出任务
     *
     * @param targetPackageName 目标进程包名
     * @param job               任务内容
     */
    void sendJob(String targetPackageName,
                 Job job);

    /**
     * 进程间通信注册/注销事件 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param eventWatcher      是否监听事件
     */
    void sendEventWatcher(String targetPackageName,
                          EventWatcher eventWatcher);

    /**
     * 进程间发送事件
     *
     * @param targetPackageName 目标进程包名
     * @param event             事件
     */
    void sendEvent(String targetPackageName,
                   Event event);

    /**
     * 进程间通信发出消息
     *
     * @param targetPackageName 目标进程包名
     * @param data              消息内容
     */
    void sendData(String targetPackageName,
                  String data);


    /**
     * 进程间通信发出消息
     *
     * @param targetPackageName 目标进程包名
     * @param data              消息内容
     */
    void sendMessage(String targetPackageName,
                     JSONObject data);

}
