package com.mango.puppet.dispatch.job;

import android.util.Log;

import com.mango.puppet.dispatch.job.db.DBManager;
import com.mango.puppet.plugin.PluginManager;
import com.mango.puppet.plugin.i.IPluginJob;
import com.mango.puppetmodel.Job;

import java.util.ArrayList;

/**
 * ExecutorManager
 *
 * 任务执行者
 *
 * @author: hehongzhen
 * @date: 2020/05/28
 */
public class ExecutorManager implements DBManager.OnJobDBChangeListener, IPluginJob.IPluginJobCallBack {
    private static final ExecutorManager ourInstance = new ExecutorManager();

    public static ExecutorManager getInstance() {
        return ourInstance;
    }

    // 是否下发了任务
    private boolean isDistributedJob = false;

    private ExecutorManager() {
    }

    /**
     * 1 监听数据库变化
     * 2 判断是否执行任务
     */
    void start() {
        DBManager.addJobDbListener(ExecutorManager.this);
        isDistributedJob = false;
        boolean flag = canDistributeJob();
        if (flag) {
            distributeJob(true);
        }
    }

    /* DBManager.OnJobDBChangeListener */
    @Override
    public void onJobInsert(Job job) {
        boolean flag = canDistributeJob();
        if (flag) {
            Log.d("exejob", "onJobInsert " + job.job_id);
            distributeJob();
        }
    }

    @Override
    public void onJobDelete(Job job) {
        boolean flag = canDistributeJob();
        if (flag) {
            Log.d("exejob", "onJobDelete " + job.job_id);
            distributeJob();
        }
    }

    @Override
    public void onJobStatusChange(Job job) {
        boolean flag = canDistributeJob();
        if (job.job_status != 1 &&  flag) {
            Log.d("exejob", "onJobStatusChange " + job.job_id + " " + job.job_status);
            distributeJob();
        }
    }

    /* IPluginJob.IPluginJobCallBack */
    @Override
    public void onFinished(Job job, boolean isSucceed, String failReason) {
        if (!isSucceed) {
            isDistributedJob = false;
            Log.d("exejob", "isDistributedJob false onFinished " + job.job_id);
            job.job_status = 0;
            boolean b = DBManager.updateJobStatus(job);
            if (!b) {
                Log.e("JobManager", "DBManager.updateJobStatus error ");
            }
        }
    }

    void receiveJobResult(Job job) {
        isDistributedJob = false;
        Log.d("exejob", "isDistributedJob false receiveJobResult" + job.job_id);
    }

    /* private */

    private boolean canDistributeJob() {
        if (!PluginManager.getInstance().isAllPluginRun()) {
            return false;
        }
        if (DBManager.getJobsDependOnStatus(5).size() > 0 ||
                DBManager.getJobsDependOnStatus(6).size() > 0) {
            StringBuilder jobid= new StringBuilder();
            ArrayList<Job> list=DBManager.getJobsDependOnStatus(6);
            for (int i = 0; i < list.size(); i++) {
                jobid.append(list.get(i).job_id).append(";");
            }
            JobManager.getInstance().setStatus(jobid.toString(),JobManager.STATUS.STOP);
            return false;
        } else if (DBManager.getReportJobs().size() > 5) {
            JobManager.getInstance().setStatus("", JobManager.STATUS.WAIT);
            return false;
        } else if (isDistributedJob) {
            JobManager.getInstance().setStatus("", JobManager.STATUS.RUNNING);
            return false;
        } else {
            return true;
        }
    }

    private void distributeJob() {
        distributeJob(false);
    }

    private void distributeJob(boolean isInit) {
        Job currentJob = isInit ?
                DBManager.getSingleNotDoneJobsFromDb() : DBManager.getSingleNewJobFromDb();
        if (currentJob != null) {
            Log.d("exejob", "onJobExe " + currentJob.job_id);
            JobManager.getInstance().setStatus("", JobManager.STATUS.RUNNING);
            isDistributedJob = true;
            PluginManager.getInstance().distributeJob(currentJob, this);
        } else {
            JobManager.getInstance().setStatus("", JobManager.STATUS.REST);
        }
    }
}
