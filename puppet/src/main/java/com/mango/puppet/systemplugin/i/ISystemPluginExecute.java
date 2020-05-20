package com.mango.puppet.systemplugin.i;

/**
 * ISystemPluginExecute
 * 执行类接口
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ISystemPluginExecute {

    /**
     * root权限执行命令行
     *
     * @param commandString 内容
     * @return 结果 0成功 非0失败
     */
    int execRootCmd(String commandString);

    /**
     * root权限执行命令行并输出结果
     *
     * @param commandString 内容
     * @return 结果
     */
    String execRootCmdWithResult(String commandString);

    /**
     * 更改前台应用
     *
     * @param packageName 要切换到前台的应用的包名
     * @param activityName 要切换到前台的应用的activity名称
     * @param result 执行结果
     */
    void changeForegroundApplication(String packageName,
                                     String activityName,
                                     ISystemPluginResult result);

    /**
     * 安装应用
     *
     * @param apkPath 要安装apk的路径
     * @param result 执行结果
     */
    void installApplication(String apkPath,
                            ISystemPluginResult result);

    /**
     * 卸载应用
     *
     * @param packageName 要卸载的应用的包名
     * @param result 执行结果
     */
    void uninstallApplication(String packageName,
                            ISystemPluginResult result);

    /**
     * kill应用
     *
     * @param packageName 要kill应用的包名
     * @param result 执行结果
     */
    void exitApplication(String packageName,
                         ISystemPluginResult result);

    /**
     * 重启应用
     *
     * @param packageName 要重启应用的包名
     * @param activityName 要重启应用的activity名称
     * @param result 执行结果
     */
    void restartApplication(String packageName,
                            String activityName,
                            ISystemPluginResult result);

    /**
     * ISystemPlugin结果回调
     */
    interface ISystemPluginResult {

        /**
         * ISystemPlugin结果回调
         * @param isSucceed 是否成功
         * @param failReason 失败原因 成功时为空
         */
        void onFinished(boolean isSucceed, String failReason);
    }
}
