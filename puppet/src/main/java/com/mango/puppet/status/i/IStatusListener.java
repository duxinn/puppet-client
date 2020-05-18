package com.mango.puppet.status.i;

/**
 * IStatusListener
 * 监听状态的改变 供其他模块实现
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IStatusListener {

    /**
     * 网络状态发生变化
     * @param isNetOk 网络是否OK
     */
    void onNetworkStatusChanged(boolean isNetOk);

    /**
     * 任务引擎状态改变
     * @param status 任务引擎状态
     */
    void onJobEngineStatusChanged(int status);

    /**
     * 待执行任务数发生改变
     * @param count 待执行任务数
     */
    void onJobCountChanged(int count);

    /**
     * 待上报任务数发生改变
     * @param count 待上报任务数
     */
    void onJobResultCountChanged(int count);

    /**
     * 注册/注销状态发生改变
     */
    void onEventWatcherChanged();

    /**
     * 插件运行状态发生改变
     */
    void onPluginRunningChanged();
}
