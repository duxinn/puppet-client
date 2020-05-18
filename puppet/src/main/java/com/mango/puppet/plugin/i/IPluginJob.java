package com.mango.puppet.plugin.i;

import com.mango.puppetmodel.Job;

/**
 * IPluginJob
 * job相关接口
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IPluginJob {

    /**
     * 将任务下发给插件 需先把目标应用切换至前台
     *
     * @param job 任务
     * @param result 是否下发成功
     */
    void distributeJob(Job job, IPluginControl.IPluginControlResult result);
}
