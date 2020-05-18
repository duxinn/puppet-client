package com.mango.puppet.status.i;

import java.util.List;
import java.util.Map;

/**
 * IStatusControl
 * 监控各模块运行状态
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IStatusControl {

    /**
     * 设置网络连接状态
     * @param status 负责网络模块的同学补充
     */
    void setNetworkStatus(int status);

    /**
     * 查看网络连接状态
     * @return 负责网络模块的同学补充
     */
    int getNetworkStatus();

    /**
     * 设置任务引擎状态 等待新任务/已停止/进行中
     * @param status 负责任务模块的同学补充
     */
    void setJobEngineStatus(int status);

    /**
     * 查看任务引擎状态 等待新任务/已停止/进行中
     * @return 负责任务模块的同学补充
     */
    int getJobEngineStatus();

    /**
     * 设置待执行任务数
     * @param count 待执行任务数
     */
    void setJobCount(int count);

    /**
     * 查看待执行任务数
     * @return 待执行任务数
     */
    int getJobCount();

    /**
     * 设置待上报任务数
     * @param count 待上报任务数
     */
    void setJobResultCount(int count);

    /**
     * 查看待上报任务数
     * @return 待上报任务数
     */
    int getJobResultCount();

    /**
     * 设置注册/注销事件的监听状态
     *
     * @param packageName 目标进程包名
     * @param eventName 事件名
     * @param isvalid true注册 false注销
     */
    void setEventWatcher(String packageName,
                         String eventName,
                         boolean isvalid);

    /**
     * 查看事件是否被监听
     *
     * @param packageName 目标进程包名
     * @param eventName 事件名
     * @return 是否生效
     */
    boolean getEventWatcher(String packageName,
                          String eventName);

    /**
     * 查看目标应用所有被监听的事件
     *
     * @param packageName 目标进程包名
     * @return 生效的监听的事件的集合
     */
    List<String> getApplicationEventWatcher(String packageName);

    /**
     * 查看所有被监听的事件
     *
     * @return 生效的监听的事件的集合 key为包名 value为监听的事件的集合
     */
    Map<String, List<String>> getAllEventWatcher();

    /**
     * 查看某个进程的插件是否运行
     *
     * @param packageName 目标进程包名
     * @return 插件是否运行
     */
    boolean isPluginRunning(String packageName);


    /**
     * 获取所有正在运行的插件的所在应用
     *
     * @return 运行插件所在应用的包名集合
     */
    List<String> getAllRunningPlugin();

    /**
     * 设置某个进程中的插件是否正在运行
     *
     * @param packageName 目标进程包名
     * @param isRunning 插件是否运行
     */
    void setPluginRunning(String packageName,
                          boolean isRunning);
}
