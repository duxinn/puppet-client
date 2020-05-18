package com.mango.puppet.dispatch.system.i;

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
     * 1 调用插件管理获取手机系统及被控应用的状态
     * 2 通知远程控制器已启动 启动local server/长连接
     * 3 通知任务模块 恢复缓存 开始队列
     * 4 通知事件模块 恢复已注册事件 开始监听新事件
     */
    void startSystem();
}
