package com.mango.puppet.plugin.i;

import android.content.Context;

import com.mango.puppet.bean.PluginModel;

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
     */
    void runPuppetPlugin();

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
