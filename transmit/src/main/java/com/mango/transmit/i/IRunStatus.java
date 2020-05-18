package com.mango.transmit.i;

/**
 * IRunStatus
 * 监听木马程序运行状态
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IRunStatus {

    /**
     * 监听目标程序运行状态 默认超时时间为2000ms
     *
     * @param targetPackageName 目标进程包名
     * @param callback          运行状态变化时
     */
    void setStatusObserver(String targetPackageName,
                           IRunStatusCallback callback);

    /**
     * 监听目标程序运行状态
     *
     * @param targetPackageName 目标进程包名
     * @param callback          运行状态变化时
     * @param timeoutMillis     超时时间 即多长时间未响应即认为木马程序已经停止运行
     */
    void setStatusObserver(String targetPackageName,
                           IRunStatusCallback callback,
                           long timeoutMillis);

    /**
     * 监听木马程序运行状态的结果回调
     */
    interface IRunStatusCallback {

        /**
         * 目标程序已连接 仅当第一次连接时调用
         *
         * @param targetPackageName 目标进程包名
         */
        void onConnected(String targetPackageName);

        /**
         * 目标程序已断开连接
         *
         * @param targetPackageName 目标进程包名
         */
        void onDisconnected(String targetPackageName);
    }
}
