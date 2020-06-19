package com.mango.puppet.dispatch.job;

import android.content.Context;
import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.dispatch.job.i.IJob;
import com.mango.puppet.log.LogManager;
import com.mango.puppet.status.StatusManager;
import com.mango.puppet.tool.ThreadUtils;
import com.mango.puppetmodel.Job;

import org.json.JSONObject;

/**
 * JobManager
 *
 * @author: hehongzhen
 * @date: 2020/05/18
 */
@SuppressWarnings("unused")
public class JobManager implements IJob {
    private static final JobManager ourInstance = new JobManager();

    enum STATUS {
        /**
         * status rest 等待新任务 如果有新任务则立刻执行
         */
        REST(0),
        /**
         * status stop 失败任务而停止执行
         */
        STOP(-1),
        /**
         * status wait 上报失败过多而停止执行
         */
        WAIT(-2),
        /**
         * status running 执行中
         */
        RUNNING(1);
        private int code;

        STATUS(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }

    private STATUS status = STATUS.STOP;


    public static JobManager getInstance() {
        return ourInstance;
    }

    private JobManager() {

    }

    /************   IJob   ************/
    @Override
    public boolean startJobSystem(Context context) {
        LogManager.getInstance().recordDebugLog("启动任务系统");
        DBManager.init(context);
        ReportManager.getInstance().start();
        ExecutorManager.getInstance().start();
        return true;
    }

    @Override
    public boolean addJob(final Job job) {
        LogManager.getInstance().recordLog("接收一个新的待执行任务" + job.job_id);
        Job storeJob = DBManager.getJobsById(job.job_id);
        if (storeJob != null) {
            LogManager.getInstance().recordLog("任务已存在 " + job.job_id);
            return false;
        }
        ThreadUtils.runInMainThread(new Runnable() {
            @Override
            public void run() {
                if (Job.RETRY_JOB.equals(job.job_name)
                        || Job.CANCEL_JOB.equals(job.job_name)) {
                    if (status == STATUS.STOP) {
                        long job_id;
                        if (Job.RETRY_JOB.equals(job.job_name)) {
                            job_id = job.job_data.optLong("retry_job_id");
                        } else {
                            job_id = job.job_data.optLong("cancel_job_id");
                        }

                        Job targetJob = DBManager.getJobsById(job_id);
                        if (targetJob != null && targetJob.job_status == 6) {
                            job.job_status = 3;
                            ReportManager.getInstance().reportToServiceNoCallback(job);

                            if (Job.RETRY_JOB.equals(job.job_name)) {
                                targetJob.job_status = 0;
                                targetJob.result_data = (new JSONObject()).toString();
                                targetJob.error_code = 0;
                                targetJob.error_message = "";
                                DBManager.updateJobStatus(targetJob);

                            } else {
                                DBManager.deleteJob(job_id);
                            }
                        } else {
                            job.job_status = 4;
                            job.error_message = "任务引擎异常";
                            job.error_code = 2;
                            ReportManager.getInstance().reportToServiceNoCallback(job);
                        }
                    } else {
                        job.job_status = 4;
                        job.error_message = "当前没有失败任务";
                        job.error_code = 1;
                        ReportManager.getInstance().reportToServiceNoCallback(job);
                    }
                } else {
                    boolean b = DBManager.insertJobIntoDb(job);
                    if (!b) {
                        Log.e("JobManager", "DBManager.insertJobIntoDb error ");
                    }
                }
            }
        });
        return true;
    }

    @Override
    public void receiveJobResult(Job jobResult) {
        LogManager.getInstance().recordLog("接收到一个执行完毕的任务" + jobResult.job_id);
        ExecutorManager.getInstance().receiveJobResult();
        Log.d("JobManager", "receiveJobResult job_status:" + jobResult.job_status);
        boolean b = DBManager.updateJobStatus(jobResult);
        if (!b) {
            Log.e("JobManager", "DBManager.updateJobStatus error ");
        }
    }

    protected void setStatus(String job_id, STATUS s) {
        status = s;
        StatusManager.getInstance().setJobEngineStatus(job_id,status.getCode());
    }
}
