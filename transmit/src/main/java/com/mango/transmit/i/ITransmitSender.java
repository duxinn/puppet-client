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
     * 进程间通信发出任务 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param job               任务内容
     */
    void sendJob(String targetPackageName,
                 String messageId,
                 ITransmitCallback callback,
                 Job job);

    /**
     * 进程间通信发出任务
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param job               任务内容
     * @param timeoutMillis     设置超时时间
     */
    void sendJob(String targetPackageName,
                 String messageId,
                 ITransmitCallback callback,
                 Job job,
                 long timeoutMillis);

    /**
     * 进程间通信注册/注销事件 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param eventWatcher      是否监听事件
     */
    void sendEventWatcher(String targetPackageName,
                          String messageId,
                          ITransmitCallback callback,
                          EventWatcher eventWatcher);

    /**
     * 进程间通信注册/注销事件
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param eventWatcher      是否监听事件
     * @param timeoutMillis     设置超时时间
     */
    void sendEventWatcher(String targetPackageName,
                          String messageId,
                          ITransmitCallback callback,
                          EventWatcher eventWatcher,
                          long timeoutMillis);

    /**
     * 进程间发送事件 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param event             事件
     */
    void sendEvent(String targetPackageName,
                   String messageId,
                   ITransmitCallback callback,
                   Event event);

    /**
     * 进程间发送事件
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param event             事件
     * @param timeoutMillis     设置超时时间
     */
    void sendEvent(String targetPackageName,
                   String messageId,
                   ITransmitCallback callback,
                   Event event,
                   long timeoutMillis);

    /**
     * 进程间通信发出消息 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param data              消息内容
     */
    void sendData(String targetPackageName,
                  String messageId,
                  ITransmitCallback callback,
                  byte[] data);

    /**
     * 进程间通信发出消息
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param data              消息内容
     * @param timeoutMillis     设置超时时间
     */
    void sendData(String targetPackageName,
                  String messageId,
                  ITransmitCallback callback,
                  byte[] data,
                  long timeoutMillis);


    /**
     * 进程间通信发出消息 默认超时时间200ms
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param data              消息内容
     */
    void sendMessage(String targetPackageName,
                     String messageId,
                     ITransmitCallback callback,
                     JSONObject data);


    /**
     * 进程间通信发出消息
     *
     * @param targetPackageName 目标进程包名
     * @param messageId         消息的唯一id 与callback回调中的messageId相同
     * @param callback          发出消息的回执
     * @param data              消息内容
     * @param timeoutMillis     设置超时时间
     */
    void sendMessage(String targetPackageName,
                     String messageId,
                     ITransmitCallback callback,
                     JSONObject data,
                     long timeoutMillis);


    /**
     * 进程间通信的结果回调
     */
    interface ITransmitCallback {

        /**
         * 发送成功
         *
         * @param messageId 消息id
         */
        void onSuccess(String messageId);

        /**
         * 发送超时
         *
         * @param messageId 消息id
         */
        void onTimeout(String messageId);
    }
}
