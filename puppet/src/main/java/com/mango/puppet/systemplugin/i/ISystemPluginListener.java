package com.mango.puppet.systemplugin.i;

/**
 * ISystemPluginListener
 * 监听类接口    使用该模块的其他模块负责实现
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ISystemPluginListener {

    /**
     * 前台应用发生改变时调用
     * @param packageName 当前前台应用的包名
     */
    void onForegroundApplicationChange(String packageName);

    /**
     * 电池电量改变时调用
     * @param intLevel 当前电量
     * @param intScale 总电量
     */
    void onBatteryChange(int intLevel, int intScale);

    /**
     * 息屏/亮屏时调用
     * @param isOff 是否息屏
     */
    void onScreenChange(boolean isOff);
}
