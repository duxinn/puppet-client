package com.mango.puppet.plugin.i;

import android.content.Context;

import com.mango.puppet.plugin.PluginModel;
import com.mango.puppet.systemplugin.i.ISystemPluginExecute;

import java.util.ArrayList;
import java.util.List;

/**
 * IPluginControl
 * 插件管理
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IPluginControl {
    
    /**
     * 启动系统
     * @param context context
     * @param pluginModels 所有插件的实体类
     * @param result 启动是否成功的回调
     */
    void startPluginSystem(Context context, List<PluginModel> pluginModels, IPluginControlResult result);

    /**
     * 运行木马插件
     *
     * @param context context
     * @param targetPackageName 目标app包名
     * @param dexName assets 目录下插件名称
     * @param className 要在目标app中运行入口类名
     * @param methodName 该类名下要调用的静态方法的方法名 注:必须是静态方法
     * @param result 运行结果
     */
    void runPuppetPlugin(Context context,
                         String targetPackageName,
                         String dexName,
                         String className,
                         String methodName,
                         IPluginControlResult result);

    /**
     * 获取可使用的插件 注:需已经安装目标app且版本正确
     *
     * @return 获取可使用的插件的目标app的包名集合
     */
    ArrayList<String> getSupportPuppetPlugin();

    /**
     * 正在运行的插件
     *
     * @return 正在运行的插件所在目标app的包名集合
     */
    ArrayList<String> getRunningPuppetPlugin();

    /**
     * 某个进程内的木马是否在运行
     *
     * @param packageName 目标app包名
     * @param result 结果
     */
    void isPluginRunning(String packageName,
                         IPluginControlResult result);

    /**
     * IPluginControl
     */
    interface IPluginControlResult {

        /**
         * IPluginControl
         * @param isSucceed 是否成功
         * @param failReason 失败原因 成功时为空
         */
        void onFinished(boolean isSucceed, String failReason);
    }
}
