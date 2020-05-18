package com.mango.puppet.log.i;

/**
 * ILog
 * 日志相关
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ILog {

    /**
     * 设置监听新日志的listener
     *
     * @param listener 监听者
     */
    void setNewLogListener(ILogListener listener);

    /**
     * 记录日志
     *
     * @param log 日志内容
     */
    void recordLog(String log);

    /**
     * 新日志监听者
     */
    interface ILogListener {

        /**
         * 有新log
         * @param log 日志内容
         */
        void onNewLog(String log);
    }
}
