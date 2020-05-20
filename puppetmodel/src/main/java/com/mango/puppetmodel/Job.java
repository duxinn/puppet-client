package com.mango.puppetmodel;

import java.util.Map;

/**
 * Job
 * 任务数据模型
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class Job {

    // 由远程服务端生成的任务唯一标识
    public long job_id;

    // 任务在哪个应用上执行，系统任务不需此参数
    public String package_name;

    // 任务名称 标识是什么任务
    public String job_name;

    // 任务状态 0:待执行 1:正在执行 2:执行成功 3:已取消 4:执行失败
    public int job_status;

    // 任务执行失败时的错误码 任务成功时为0
    public int error_code;

    // 任务执行失败时的原因 任务成功时为null
    public String error_message;

    // 任务所需参数
    public Map job_data;

    // 任务执行结果附带参数
    public Map result_data;


}
