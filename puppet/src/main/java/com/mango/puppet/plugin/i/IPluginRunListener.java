package com.mango.puppet.plugin.i;

/**
 * IPluginRunListener
 * 木马插件运行状态变化的监听者 供其他模块实现
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IPluginRunListener {

    /**
     * 木马插件运行状态变化时调用
     *
     * @param packageName 木马所在应用的包名
     * @param isRunning 是否在运行
     */
    void onPluginRunningStatusChange(String packageName,
                                     boolean isRunning);
}
