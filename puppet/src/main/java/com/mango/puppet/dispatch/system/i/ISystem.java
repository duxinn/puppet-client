package com.mango.puppet.dispatch.system.i;

import android.content.Context;

/**
 * ISystem
 * 系统调度
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface ISystem {

    /**
     * 启动程序
     * 1 插件管理模块
     *      检查是否root及是否有读写权限
     *      根据插件管理层暴露的接口 检查目标app是否安装、破解插件是否存在、破解插件的版本和app版本是否一致
     *      启动数据传输模块
     *      运行木马程序
     *
     * 2 任务模块
     *      启动任务执行模块
     *      启动任务结果上报模块
     *
     * 3 事件模块
     *      从本地恢复已注册的事件
     *      通知插件管理模块所有有效的事件
     *
     * 4 启动本地server
     */
    void startSystem(Context context);
}
