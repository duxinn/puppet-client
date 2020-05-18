package com.mango.transmit.i;

import com.mango.puppetmodel.Event;

/**
 * IEventTransform
 * 将目标程序原始数据转化为业务数据
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IEventTransform {

    /**
     * 将目标程序原始数据转化为业务数据
     *
     * @param packageName 原始数据从属应用的包名
     * @param event 原始数据
     * @param callback 转换结果
     */
    void transformEvent(String packageName,
                        Event event,
                        IIEventTransformCallback callback);

    /**
     * 转换完成callback
     */
    interface IIEventTransformCallback {

        /**
         * 转换完成
         *
         * @param transformedEvent 已转换后的数据
         */
        void onFinished(Event transformedEvent);
    }
}
