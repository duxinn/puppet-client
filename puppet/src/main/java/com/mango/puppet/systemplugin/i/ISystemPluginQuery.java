package com.mango.puppet.systemplugin.i;

/**
 * ISystemPluginQuery
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ISystemPluginQuery {

    /**
     * 获取当前前台应用
     * @return 当前前台应用包名
     */
    String getCurrentForegroundApplication();

    /**
     * 检测手机是否root
     *
     * @return true为已root
     */
    boolean hasRootPermission();

    /**
     * 获取目标应用版本号
     *
     * @param packageName 目标应用包名
     * @return 目标应用版本号 空为获取失败或者未安装
     */
    String getApplicationVersion(String packageName);
}
