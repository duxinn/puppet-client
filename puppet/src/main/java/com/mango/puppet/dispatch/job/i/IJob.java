package com.mango.puppet.dispatch.job.i;

import android.content.Context;

import com.mango.puppetmodel.Job;

/**
 * IJob
 * 任务调度
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public interface IJob {

    /**
     * 接收一个新的待执行任务
     * @param job 待执行任务
     */
    boolean addJob(Job job);

    /**
     * 接收一个执行完毕的任务
     * @param jobResult 任务执行结果
     */
    void receiveJobResult(Job jobResult);


    /**
     * 启动系统
     * @return 启动是否成功
     */
    boolean startJobSystem(Context context);
}
